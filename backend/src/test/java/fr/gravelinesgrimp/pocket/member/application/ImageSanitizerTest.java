package fr.gravelinesgrimp.pocket.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

class ImageSanitizerTest {

    private final ImageSanitizer sanitizer = new ImageSanitizer();

    @Test
    void cropsToACenteredSquareAndShrinksIt() throws IOException {
        var clean = sanitizer.squareJpeg(image("png", 800, 600), 512);

        assertThat(clean.width()).isEqualTo(512);
        assertThat(clean.height()).isEqualTo(512);
        var decoded = ImageIO.read(new ByteArrayInputStream(clean.jpeg()));
        assertThat(decoded.getWidth()).isEqualTo(512);
        assertThat(ImageSanitizer.detect(clean.jpeg())).contains(ImageSanitizer.ImageType.JPEG);
    }

    @Test
    void neverEnlargesASmallPhoto() throws IOException {
        var clean = sanitizer.squareJpeg(image("png", 100, 80), 512);
        assertThat(clean.width()).isEqualTo(80);
    }

    @Test
    void removesExifMetadataSuchAsTheGpsPosition() throws IOException {
        var withExif = withExifBlock(image("jpeg", 640, 480));
        assertThat(contains(withExif, "Exif")).isTrue();

        var clean = sanitizer.squareJpeg(withExif, 512);

        assertThat(contains(clean.jpeg(), "Exif")).isFalse();
        assertThat(contains(clean.jpeg(), "GPS")).isFalse();
    }

    @Test
    void judgesTheTypeFromTheBytesNotTheName() {
        var html = "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8);
        assertRefused(html, ErrorCode.IMAGE_UNSUPPORTED);
        assertThat(ImageSanitizer.detect("RIFF0000WEBPVP8 ".getBytes(StandardCharsets.US_ASCII)))
                .contains(ImageSanitizer.ImageType.WEBP);
    }

    @Test
    void refusesACorruptedImage() throws IOException {
        var truncated = Arrays.copyOf(image("png", 300, 300), 60);
        assertRefused(truncated, ErrorCode.IMAGE_UNSUPPORTED);
    }

    @Test
    void refusesHugeImagesBeforeDecodingThem() {
        assertRefused(pngHeaderOnly(10_000, 10_000), ErrorCode.IMAGE_TOO_LARGE);
        // 4400 × 4000: 17.6 megapixels.
        assertRefused(pngHeaderOnly(4_400, 4_000), ErrorCode.IMAGE_TOO_LARGE);
        // Few pixels, but a side longer than 8192.
        assertRefused(pngHeaderOnly(9_000, 100), ErrorCode.IMAGE_TOO_LARGE);
    }

    @Test
    void decodesBigPhotosSubsampledButStillSharpEnough() throws IOException {
        var clean = sanitizer.squareJpeg(image("png", 3000, 2200), 512);
        assertThat(clean.width()).isEqualTo(512);
        var decoded = ImageIO.read(new ByteArrayInputStream(clean.jpeg()));
        // The centre of the test picture is the olive disc.
        var centre = new Color(decoded.getRGB(256, 256));
        assertThat(centre.getGreen()).isGreaterThan(centre.getBlue() + 60);
    }

    @Test
    void fitsAWholePhotoWithoutCroppingIt() throws IOException {
        var clean = sanitizer.fittedJpeg(image("png", 3200, 1800), 1600);

        assertThat(clean.width()).isEqualTo(1600);
        assertThat(clean.height()).isEqualTo(900);
        var decoded = ImageIO.read(new ByteArrayInputStream(clean.jpeg()));
        assertThat(decoded.getWidth()).isEqualTo(1600);
        assertThat(decoded.getHeight()).isEqualTo(900);
        // Not cropped: the left edge is still the orange background.
        var edge = new Color(decoded.getRGB(4, 450));
        assertThat(edge.getRed()).isGreaterThan(edge.getGreen() + 100);
    }

    @Test
    void fitsPortraitPhotosByTheirHeight() throws IOException {
        var clean = sanitizer.fittedJpeg(image("jpeg", 1200, 2400), 1600);
        assertThat(clean.width()).isEqualTo(800);
        assertThat(clean.height()).isEqualTo(1600);
    }

    @Test
    void neverEnlargesASmallRoutePhoto() throws IOException {
        var clean = sanitizer.fittedJpeg(image("png", 640, 480), 1600);
        assertThat(clean.width()).isEqualTo(640);
        assertThat(clean.height()).isEqualTo(480);
    }

    @Test
    void routePhotosLoseTheirMetadataToo() throws IOException {
        var clean = sanitizer.fittedJpeg(withExifBlock(image("jpeg", 640, 480)), 1600);
        assertThat(contains(clean.jpeg(), "Exif")).isFalse();
        assertThat(contains(clean.jpeg(), "GPS")).isFalse();
    }

    @Test
    void routePhotosFollowTheSameSafetyRules() {
        var html = "<html></html>".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> sanitizer.fittedJpeg(html, 1600))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.IMAGE_UNSUPPORTED));
        assertThatThrownBy(() -> sanitizer.fittedJpeg(pngHeaderOnly(10_000, 10_000), 1600))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.IMAGE_TOO_LARGE));
    }

    private void assertRefused(byte[] upload, ErrorCode code) {
        assertThatThrownBy(() -> sanitizer.squareJpeg(upload, 512))
                .isInstanceOfSatisfying(
                        BusinessException.class, e -> assertThat(e.code()).isEqualTo(code));
    }

    private static byte[] image(String format, int width, int height) throws IOException {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(new Color(0xF9, 0x56, 0x1A));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(0xC3, 0xC0, 0x4C));
        graphics.fillOval(width / 4, height / 4, width / 2, height / 2);
        graphics.dispose();
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    /** Inserts an APP1 "Exif" segment, as a phone camera writes one, right after the SOI marker. */
    private static byte[] withExifBlock(byte[] jpeg) {
        var payload = "Exif\0\0MM\0*\0\0\0\bGPS 50.98N 2.12E".getBytes(StandardCharsets.ISO_8859_1);
        var segment = ByteBuffer.allocate(4 + payload.length);
        segment.put((byte) 0xFF).put((byte) 0xE1).putShort((short) (payload.length + 2));
        segment.put(payload);
        var result = new ByteArrayOutputStream();
        result.write(jpeg, 0, 2);
        result.writeBytes(segment.array());
        result.write(jpeg, 2, jpeg.length - 2);
        return result.toByteArray();
    }

    /** A valid PNG signature and header announcing a huge image, without any pixel data. */
    private static byte[] pngHeaderOnly(int width, int height) {
        var header = ByteBuffer.allocate(13).putInt(width).putInt(height);
        header.put((byte) 8).put((byte) 2).put((byte) 0).put((byte) 0).put((byte) 0);
        var output = new ByteArrayOutputStream();
        output.writeBytes(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
        output.writeBytes(chunk("IHDR", header.array()));
        output.writeBytes(chunk("IEND", new byte[0]));
        return output.toByteArray();
    }

    private static byte[] chunk(String type, byte[] data) {
        var typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        var crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        return ByteBuffer.allocate(12 + data.length)
                .putInt(data.length)
                .put(typeBytes)
                .put(data)
                .putInt((int) crc.getValue())
                .array();
    }

    private static boolean contains(byte[] bytes, String text) {
        var needle = text.getBytes(StandardCharsets.US_ASCII);
        for (var i = 0; i <= bytes.length - needle.length; i++) {
            if (Arrays.equals(bytes, i, i + needle.length, needle, 0, needle.length)) {
                return true;
            }
        }
        return false;
    }
}
