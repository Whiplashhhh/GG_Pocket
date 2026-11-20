package fr.gravelinesgrimp.pocket.notification.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import fr.gravelinesgrimp.pocket.FriendshipFixtures;
import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.notification.config.PushRunner;
import fr.gravelinesgrimp.pocket.notification.domain.PushGateway;
import fr.gravelinesgrimp.pocket.notification.domain.WebPush;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Push (D-128, D-130): my devices and preferences are mine only; a comment on my session reaches my
 * phone, encrypted for it, if I want it. Sent on the spot here, to a gateway that keeps them.
 */
@IntegrationTest
@Import(PushIT.Wiring.class)
@TestPropertySource(
        properties = {
            "gg.push.public-key="
                    + "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8",
            "gg.push.private-key=yfWPiYE-n46HLnH0KqZOF1fJJU3MYrct3AELtAQ-oRw",
            "gg.push.subject=mailto:club@example.org"
        })
class PushIT {

    record Sent(String endpoint, byte[] body, String authorization) {}

    @TestConfiguration
    static class Wiring {

        static final List<Sent> SENT = Collections.synchronizedList(new ArrayList<>());

        @Bean
        @Primary
        PushGateway keepingGateway() {
            return (device, service, body, authorization) -> {
                SENT.add(new Sent(device.endpoint(), body, authorization));
                return PushGateway.Outcome.DELIVERED;
            };
        }

        @Bean
        @Primary
        PushRunner onTheSpot() {
            return Runnable::run;
        }
    }

    /** A browser's side of a subscription: its key pair and secret. */
    record Device(String endpoint, KeyPair keys, byte[] auth) {

        static Device create() throws Exception {
            var generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            var auth = new byte[16];
            new SecureRandom().nextBytes(auth);
            return new Device(
                    "https://fcm.googleapis.com/fcm/send/" + UUID.randomUUID(),
                    generator.generateKeyPair(),
                    auth);
        }

        byte[] publicKey() {
            return WebPush.raw((ECPublicKey) keys.getPublic());
        }

        String json() {
            return "{\"endpoint\": \""
                    + endpoint
                    + "\", \"keys\": {\"p256dh\": \""
                    + WebPush.encode(publicKey())
                    + "\", \"auth\": \""
                    + WebPush.encode(auth)
                    + "\"}}";
        }

        /** RFC 8291 from the receiving end. */
        String read(byte[] body) throws Exception {
            var buffer = ByteBuffer.wrap(body);
            var salt = new byte[16];
            buffer.get(salt);
            buffer.getInt();
            var serverPublic = new byte[buffer.get()];
            buffer.get(serverPublic);
            var ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            var agreement = KeyAgreement.getInstance("ECDH");
            agreement.init(keys.getPrivate());
            agreement.doPhase(WebPush.publicKey(serverPublic), true);
            var prkKey = hmac(auth, agreement.generateSecret());
            var info = new ByteArrayOutputStream();
            info.writeBytes("WebPush: info\0".getBytes(US_ASCII));
            info.writeBytes(publicKey());
            info.writeBytes(serverPublic);
            var ikm = expand(prkKey, info.toByteArray(), 32);
            var prk = hmac(salt, ikm);
            var cek = expand(prk, "Content-Encoding: aes128gcm\0".getBytes(US_ASCII), 16);
            var nonce = expand(prk, "Content-Encoding: nonce\0".getBytes(US_ASCII), 12);
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(cek, "AES"),
                    new GCMParameterSpec(128, nonce));
            var padded = cipher.doFinal(ciphertext);
            assertThat(padded[padded.length - 1]).isEqualTo((byte) 2);
            return new String(padded, 0, padded.length - 1, UTF_8);
        }

        private static byte[] hmac(byte[] key, byte[] data) throws Exception {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        }

        private static byte[] expand(byte[] prk, byte[] info, int length) throws Exception {
            var input = Arrays.copyOf(info, info.length + 1);
            input[info.length] = 1;
            return Arrays.copyOf(hmac(prk, input), length);
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @BeforeEach
    void forgetWhatWasSent() {
        Wiring.SENT.clear();
    }

    @Test
    void myDevicesAndPreferencesAreMine() throws Exception {
        var me = members.create(Role.TOURIST);
        var other = members.create(Role.CLIMBER);
        var device = Device.create();

        settings(me, device)
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.publicKey")
                                .value(
                                        "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmY"
                                                + "WAmS6TlzAC8wEqKK6PBru3jl7A8"))
                .andExpect(jsonPath("$.preferences.comments").value(true))
                .andExpect(jsonPath("$.preferences.reactions").value(false))
                .andExpect(jsonPath("$.thisDevice").value(false));

        addDevice(me, device.json()).andExpect(status().isNoContent());
        settings(me, device).andExpect(jsonPath("$.thisDevice").value(true));
        // Someone else cannot remove my device: nothing happens.
        removeDevice(other, device).andExpect(status().isNoContent());
        settings(me, device).andExpect(jsonPath("$.thisDevice").value(true));
        settings(other, device).andExpect(jsonPath("$.thisDevice").value(false));
        removeDevice(me, device).andExpect(status().isNoContent());
        settings(me, device).andExpect(jsonPath("$.thisDevice").value(false));

        mockMvc.perform(
                        put("/api/v1/me/push/preferences")
                                .with(as(me))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"comments\": false, \"replies\": true, \"reactions\":"
                                                + " true, \"friendSessions\": false,"
                                                + " \"friendRequests\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reactions").value(true));
        settings(me, device)
                .andExpect(jsonPath("$.preferences.comments").value(false))
                .andExpect(jsonPath("$.preferences.reactions").value(true));
        settings(other, device).andExpect(jsonPath("$.preferences.reactions").value(false));
    }

    @Test
    void onlyThePushServicesOfTheBrowsersAndRealKeys() throws Exception {
        var me = members.create(Role.CLIMBER);
        var device = Device.create();
        var keys =
                "\"keys\": {\"p256dh\": \""
                        + WebPush.encode(device.publicKey())
                        + "\", \"auth\": \""
                        + WebPush.encode(device.auth())
                        + "\"}";

        addDevice(me, "{\"endpoint\": \"https://evil.example/push\", " + keys + "}")
                .andExpect(status().isBadRequest());
        addDevice(me, "{\"endpoint\": \"http://web:8081/fcm/x\", " + keys + "}")
                .andExpect(status().isBadRequest());
        addDevice(
                        me,
                        "{\"endpoint\": \""
                                + device.endpoint()
                                + "\", \"keys\": {\"p256dh\": \"bm90IGEga2V5\", \"auth\":"
                                + " \"bm90IGEga2V5\"}}")
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/me/push")).andExpect(status().isUnauthorized());
    }

    @Test
    void aCommentOnMySessionReachesMyPhoneEncryptedForIt() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(friend, me);
        var device = Device.create();
        addDevice(me, device.json()).andExpect(status().isNoContent());
        var mine = shared(me);

        comment(friend, mine, "Propre, \"le crux\" !").andExpect(status().isCreated());

        assertThat(Wiring.SENT).hasSize(1);
        var sent = Wiring.SENT.getFirst();
        assertThat(sent.endpoint()).isEqualTo(device.endpoint());
        assertThat(sent.authorization())
                .startsWith("vapid t=")
                .endsWith(
                        ", k=BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8"
                                + "wEqKK6PBru3jl7A8");
        assertThat(device.read(sent.body()))
                .isEqualTo(
                        "{\"kind\":\"COMMENT\",\"nickname\":\""
                                + friend.nickname()
                                + "\",\"climbedOn\":\""
                                + mine.climbedOn()
                                + "\",\"reaction\":null,\"excerpt\":\"Propre, \\\"le crux\\\" !\","
                                + "\"url\":\"/seances/"
                                + mine.id()
                                + "\"}");
    }

    @Test
    void whatIDoNotWantStaysOffMyPhone() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var stranger = members.create(Role.CLIMBER);
        friendships.befriend(friend, me);
        addDevice(me, Device.create().json()).andExpect(status().isNoContent());
        var mine = shared(me);

        // Reactions are off by default; my own comment rings nobody.
        react(friend, mine).andExpect(status().isOk());
        comment(me, mine, "Merci !").andExpect(status().isCreated());
        assertThat(Wiring.SENT).isEmpty();

        // A friend request is on by default.
        mockMvc.perform(put("/api/v1/me/friends/" + me.id()).with(as(stranger)).with(xsrf()))
                .andExpect(status().isOk());
        assertThat(Wiring.SENT).hasSize(1);
    }

    private ClimbingSession shared(Member climber) {
        return sessions.log(
                climber,
                ClubTime.today(clock),
                8,
                clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));
    }

    private ResultActions settings(Member member, Device device) throws Exception {
        return mockMvc.perform(
                get("/api/v1/me/push").param("endpoint", device.endpoint()).with(as(member)));
    }

    private ResultActions addDevice(Member member, String json) throws Exception {
        return mockMvc.perform(
                put("/api/v1/me/push/devices")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));
    }

    private ResultActions removeDevice(Member member, Device device) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/me/push/devices")
                        .param("endpoint", device.endpoint())
                        .with(as(member))
                        .with(xsrf()));
    }

    private ResultActions react(Member member, ClimbingSession session) throws Exception {
        return mockMvc.perform(
                put("/api/v1/sessions/" + session.id() + "/reaction")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kind\": \"FIRE\"}"));
    }

    private ResultActions comment(Member author, ClimbingSession session, String body)
            throws Exception {
        return mockMvc.perform(
                post("/api/v1/sessions/" + session.id() + "/comments")
                        .with(as(author))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"" + body.replace("\"", "\\\"") + "\"}"));
    }
}
