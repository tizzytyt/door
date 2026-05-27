package com.access.control.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

/**
 * 生成图形验证码 PNG（仅依赖 JDK）
 */
public final class CaptchaImageUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final Random RANDOM = new Random();

    private CaptchaImageUtil() {
    }

    public static String toBase64Png(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(245, 246, 250));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            for (int i = 0; i < 6; i++) {
                g.setColor(randomColor(160, 220));
                g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT),
                        RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
            }

            g.setFont(new Font("Arial", Font.BOLD, 28));
            int charWidth = WIDTH / (code.length() + 1);
            for (int i = 0; i < code.length(); i++) {
                g.setColor(randomColor(30, 120));
                double angle = (RANDOM.nextDouble() - 0.5) * 0.4;
                int x = charWidth * (i + 1) - 8;
                int y = 28 + RANDOM.nextInt(6);
                g.rotate(angle, x, y);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
                g.rotate(-angle, x, y);
            }

            for (int i = 0; i < 40; i++) {
                g.setColor(randomColor(100, 200));
                g.fillRect(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
            }
        } finally {
            g.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("生成验证码图片失败", e);
        }
    }

    private static Color randomColor(int min, int max) {
        int r = min + RANDOM.nextInt(Math.max(1, max - min));
        int g = min + RANDOM.nextInt(Math.max(1, max - min));
        int b = min + RANDOM.nextInt(Math.max(1, max - min));
        return new Color(r, g, b);
    }
}
