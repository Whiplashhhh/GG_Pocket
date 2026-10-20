package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageInputStream;

/**
 * Turns an uploaded photo into a clean JPEG: the real type is checked from the bytes (never from
 * the file name or the declared type), huge images are refused before being decoded, and the
 * picture is redrawn and re-encoded from its pixels only, so no metadata (EXIF, GPS position,
 * camera…) survives.
 *
 * <p>EXIF orientation is not applied here: the app resizes photos in the browser first, which
 * already draws them the right way up (DECISIONS D-054).
 */
@Component
public class ImageSanitizer {

    /** Beyond this, decoding alone could exhaust the memory of the server. */
    static final long MAX_PIXELS = 40_000_000L;

    private static final float JPEG_QUALITY = 0.85f;
    private static final Color PAPER = new Color(0xFF, 0xFD, 0xF8);

    /** A cleaned square JPEG. */
    public record CleanImage(byte[] jpeg, int width, int height) {}

    /** Detected from the first bytes of the file. */
    enum ImageType {
        JPEG("jpeg"),
        PNG("png"),
        WEBP("webp");

        private final String formatName;

        ImageType(String formatName) {
            this.formatName = formatName;
        }
    }

    /**
     * Center-crops the photo to a square of at most {@code size} pixels.
     *
     * @throws BusinessException {@code IMAGE_UNSUPPORTED} if this is not a readable JPEG, PNG or
     *     WebP, {@code IMAGE_TOO_LARGE} if it has too many pixels
     */
    public CleanImage squareJpeg(byte[] upload, int size) {
        var type =
                detect(upload)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.IMAGE_UNSUPPORTED,
                                                "Not a JPEG, PNG or WebP file"));
        var source = decode(upload, type);
        var side = Math.min(source.getWidth(), source.getHeight());
        var target = Math.min(side, size);
        var square =
                source.getSubimage(
                        (source.getWidth() - side) / 2,
                        (source.getHeight() - side) / 2,
                        side,
                        side);
        var resized = resize(square, target);
        return new CleanImage(encodeJpeg(resized), target, target);
    }

    static Optional<ImageType> detect(byte[] bytes) {
        if (startsWith(bytes, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of(ImageType.JPEG);
        }
        if (startsWith(bytes, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of(ImageType.PNG);
        }
        if (startsWith(bytes, 0, 'R', 'I', 'F', 'F') && startsWith(bytes, 8, 'W', 'E', 'B', 'P')) {
            return Optional.of(ImageType.WEBP);
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] bytes, int offset, int... expected) {
        if (bytes.length < offset + expected.length) {
            return false;
        }
        for (var i = 0; i < expected.length; i++) {
            if ((bytes[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static BufferedImage decode(byte[] upload, ImageType type) {
        try (ImageInputStream input =
                ImageIO.createImageInputStream(new ByteArrayInputStream(upload))) {
            var readers = ImageIO.getImageReadersByFormatName(type.formatName);
            if (input == null || !readers.hasNext()) {
                throw unsupported("No reader for " + type);
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > MAX_PIXELS) {
                    throw new BusinessException(
                            ErrorCode.IMAGE_TOO_LARGE, "Image of " + pixels + " pixels refused");
                }
                var image = reader.read(0);
                if (image == null || image.getWidth() == 0 || image.getHeight() == 0) {
                    throw unsupported("Empty image");
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            // Corrupted files make readers fail in many ways (IIOException, index errors…).
            throw unsupported("Unreadable " + type + ": " + e.getMessage());
        }
    }

    /**
     * Draws the picture on an opaque RGB canvas (transparent areas become paper white), halving the
     * size step by step for a smooth result.
     */
    private static BufferedImage resize(BufferedImage source, int target) {
        var current = toRgb(source);
        while (current.getWidth() / 2 >= target) {
            current = draw(current, current.getWidth() / 2);
        }
        return current.getWidth() == target ? current : draw(current, target);
    }

    private static BufferedImage toRgb(BufferedImage source) {
        var rgb =
                new BufferedImage(
                        source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        var graphics = rgb.createGraphics();
        try {
            graphics.setColor(PAPER);
            graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return rgb;
    }

    private static BufferedImage draw(BufferedImage source, int side) {
        var resized = new BufferedImage(side, side, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, side, side, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    /** Writes pixels only: no metadata object is handed to the writer, so none is written. */
    private static byte[] encodeJpeg(BufferedImage image) {
        var writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        var output = new ByteArrayOutputStream();
        try (var stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            var params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(image, null, null), params);
        } catch (IOException e) {
            throw new IllegalStateException("Could not encode a JPEG", e);
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }

    private static BusinessException unsupported(String message) {
        return new BusinessException(ErrorCode.IMAGE_UNSUPPORTED, message);
    }
}
