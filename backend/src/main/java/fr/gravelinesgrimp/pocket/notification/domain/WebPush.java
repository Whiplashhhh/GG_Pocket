package fr.gravelinesgrimp.pocket.notification.domain;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Web Push with the JDK alone (D-130): the message encryption of RFC 8291 ({@code aes128gcm}, RFC
 * 8188) and the VAPID token of RFC 8292. No third-party library: the maintained ones are few, and
 * this is a hundred lines checked against the RFC's own example.
 */
public final class WebPush {

    /** One record holds the whole message: 4096 bytes, tag and delimiter included. */
    static final int RECORD_SIZE = 4096;

    /** What fits in that record: 16 bytes go to the tag, 1 to the delimiter. */
    public static final int MAX_PLAINTEXT = RECORD_SIZE - 17;

    private static final ECParameterSpec P256 = p256();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DECODER = Base64.getUrlDecoder();

    private WebPush() {}

    /** The server's VAPID key pair; {@code publicKey} is what the browsers subscribe with. */
    public record VapidKeys(ECPrivateKey privateKey, byte[] publicKey) {

        /**
         * @param publicKey base64url of the uncompressed point (65 bytes)
         * @param privateKey base64url of the private scalar (32 bytes)
         */
        public static VapidKeys of(String publicKey, String privateKey) {
            var raw = decode(publicKey);
            var keys = new VapidKeys(WebPush.privateKey(decode(privateKey)), raw);
            // Two halves of different pairs would sign what no push service accepts (HTTP 403).
            try {
                var probe = "GG Pocket".getBytes(US_ASCII);
                var signer = Signature.getInstance("SHA256withECDSA");
                signer.initSign(keys.privateKey());
                signer.update(probe);
                var verifier = Signature.getInstance("SHA256withECDSA");
                verifier.initVerify(WebPush.publicKey(raw));
                verifier.update(probe);
                if (!verifier.verify(signer.sign())) {
                    throw new IllegalArgumentException(
                            "The VAPID public and private keys are not of the same pair");
                }
            } catch (GeneralSecurityException e) {
                throw new IllegalArgumentException("Invalid VAPID keys", e);
            }
            return keys;
        }

        public String publicKeyBase64() {
            return encode(publicKey);
        }
    }

    /** {@code Authorization} header of a push request (RFC 8292, « vapid » scheme). */
    public static String vapidAuthorization(
            VapidKeys keys, String audience, String subject, Instant expiresAt) {
        var header = encode("{\"typ\":\"JWT\",\"alg\":\"ES256\"}".getBytes(UTF_8));
        var claims =
                encode(
                        ("{\"aud\":"
                                        + Json.string(audience)
                                        + ",\"exp\":"
                                        + expiresAt.getEpochSecond()
                                        + ",\"sub\":"
                                        + Json.string(subject)
                                        + "}")
                                .getBytes(UTF_8));
        var signed = header + "." + claims;
        try {
            var signature = Signature.getInstance("SHA256withECDSAinP1363Format");
            signature.initSign(keys.privateKey());
            signature.update(signed.getBytes(US_ASCII));
            return "vapid t="
                    + signed
                    + "."
                    + encode(signature.sign())
                    + ", k="
                    + keys.publicKeyBase64();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("VAPID signature", e);
        }
    }

    /**
     * The body of a push request for one device: a fresh key pair and salt for each message.
     *
     * @param uaPublic the device's {@code p256dh}, raw (65 bytes)
     * @param authSecret the device's {@code auth}, raw (16 bytes)
     */
    public static byte[] encrypt(byte[] plaintext, byte[] uaPublic, byte[] authSecret) {
        try {
            var generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"), RANDOM);
            var salt = new byte[16];
            RANDOM.nextBytes(salt);
            return encrypt(plaintext, uaPublic, authSecret, generator.generateKeyPair(), salt);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Push key pair", e);
        }
    }

    /** RFC 8291 §3.4, with the server's key pair and the salt given (the RFC's example). */
    static byte[] encrypt(
            byte[] plaintext, byte[] uaPublic, byte[] authSecret, KeyPair server, byte[] salt) {
        if (plaintext.length > MAX_PLAINTEXT) {
            throw new IllegalArgumentException("Push message too long");
        }
        if (authSecret.length != 16 || salt.length != 16) {
            throw new IllegalArgumentException("Auth secret and salt are 16 bytes");
        }
        try {
            var asPublic = raw((ECPublicKey) server.getPublic());
            var agreement = KeyAgreement.getInstance("ECDH");
            agreement.init(server.getPrivate());
            agreement.doPhase(publicKey(uaPublic), true);
            var ecdhSecret = agreement.generateSecret();

            var prkKey = hmac(authSecret, ecdhSecret);
            var keyInfo = concat("WebPush: info\0".getBytes(US_ASCII), uaPublic, asPublic);
            var ikm = expand(prkKey, keyInfo, 32);
            var prk = hmac(salt, ikm);
            var cek = expand(prk, "Content-Encoding: aes128gcm\0".getBytes(US_ASCII), 16);
            var nonce = expand(prk, "Content-Encoding: nonce\0".getBytes(US_ASCII), 12);

            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(cek, "AES"),
                    new GCMParameterSpec(128, nonce));
            // A single record, so the last one: delimiter 0x02, no padding.
            var ciphertext = cipher.doFinal(concat(plaintext, new byte[] {2}));

            var header =
                    ByteBuffer.allocate(16 + 4 + 1 + asPublic.length)
                            .put(salt)
                            .putInt(RECORD_SIZE)
                            .put((byte) asPublic.length)
                            .put(asPublic)
                            .array();
            return concat(header, ciphertext);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Push encryption", e);
        }
    }

    /** A P-256 public key from its uncompressed point (0x04, X, Y). */
    public static ECPublicKey publicKey(byte[] raw) {
        if (raw.length != 65 || raw[0] != 4) {
            throw new IllegalArgumentException("Not an uncompressed P-256 point");
        }
        var point =
                new ECPoint(
                        new BigInteger(1, Arrays.copyOfRange(raw, 1, 33)),
                        new BigInteger(1, Arrays.copyOfRange(raw, 33, 65)));
        try {
            return (ECPublicKey)
                    KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(point, P256));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Not a P-256 point", e);
        }
    }

    static ECPrivateKey privateKey(byte[] scalar) {
        if (scalar.length != 32) {
            throw new IllegalArgumentException("A P-256 private key is 32 bytes");
        }
        try {
            return (ECPrivateKey)
                    KeyFactory.getInstance("EC")
                            .generatePrivate(new ECPrivateKeySpec(new BigInteger(1, scalar), P256));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Not a P-256 private key", e);
        }
    }

    /** The uncompressed point of a P-256 public key: what browsers call {@code p256dh}. */
    public static byte[] raw(ECPublicKey key) {
        return concat(
                new byte[] {4},
                fixed(key.getW().getAffineX(), 32),
                fixed(key.getW().getAffineY(), 32));
    }

    public static byte[] decode(String base64url) {
        return B64_DECODER.decode(base64url.strip().replace("=", ""));
    }

    public static String encode(byte[] bytes) {
        return B64.encodeToString(bytes);
    }

    private static byte[] fixed(BigInteger value, int length) {
        var bytes = value.toByteArray();
        var out = new byte[length];
        var copied = Math.min(bytes.length, length);
        System.arraycopy(bytes, bytes.length - copied, out, length - copied, copied);
        return out;
    }

    private static byte[] hmac(byte[] key, byte[] data) throws GeneralSecurityException {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    /** HKDF-Expand for one block (at most 32 bytes): enough for every key here. */
    private static byte[] expand(byte[] prk, byte[] info, int length)
            throws GeneralSecurityException {
        return Arrays.copyOf(hmac(prk, concat(info, new byte[] {1})), length);
    }

    private static byte[] concat(byte[]... parts) {
        var out = new ByteArrayOutputStream();
        for (var part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }

    private static ECParameterSpec p256() {
        try {
            var parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("P-256 is part of the JDK", e);
        }
    }
}
