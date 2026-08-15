package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class IconUtil {

    private static BufferedImage baseLogoImage = null;

    static {
        loadBaseImage();
    }

    private static void loadBaseImage() {
        try {
            InputStream is = IconUtil.class.getResourceAsStream("/resources/app_logo.png");
            if (is != null) {
                baseLogoImage = ImageIO.read(is);
                is.close();
            }
        } catch (Exception ignored) {}

        if (baseLogoImage == null) {
            try {
                File f1 = new File("src/resources/app_logo.png");
                if (f1.exists()) {
                    baseLogoImage = ImageIO.read(f1);
                } else {
                    File f2 = new File("bin/resources/app_logo.png");
                    if (f2.exists()) {
                        baseLogoImage = ImageIO.read(f2);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static List<Image> getAppIconImages() {
        List<Image> images = new ArrayList<>();
        int[] sizes = {16, 32, 48, 64, 128, 256, 512};
        for (int size : sizes) {
            images.add(getAppIconImage(size, size));
        }
        return images;
    }

    public static BufferedImage getAppIconImage(int width, int height) {
        if (baseLogoImage != null) {
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(baseLogoImage, 0, 0, width, height, null);
            g2.dispose();
            return scaled;
        }

        return createFallbackLogo(width, height);
    }

    public static ImageIcon getAppIcon(int width, int height) {
        return new ImageIcon(getAppIconImage(width, height));
    }

    public static void applyAppIcon(Window window) {
        if (window != null) {
            window.setIconImages(getAppIconImages());
        }
    }

    public static ImageIcon createGearIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = size / 2.0;
        double cy = size / 2.0;
        double rTeeth = size * 0.46;
        double rRoot = size * 0.32;
        double rHole = size * 0.15;
        int teethCount = 8;

        Path2D gear = new Path2D.Double(Path2D.WIND_EVEN_ODD);

        for (int i = 0; i < teethCount; i++) {
            double angle = i * (2 * Math.PI / teethCount);
            double step = (2 * Math.PI / teethCount);

            double a1 = angle - step * 0.22;
            double a2 = angle - step * 0.10;
            double a3 = angle + step * 0.10;
            double a4 = angle + step * 0.22;

            if (i == 0) {
                gear.moveTo(cx + rRoot * Math.cos(a1), cy + rRoot * Math.sin(a1));
            } else {
                gear.lineTo(cx + rRoot * Math.cos(a1), cy + rRoot * Math.sin(a1));
            }

            gear.lineTo(cx + rTeeth * Math.cos(a2), cy + rTeeth * Math.sin(a2));
            gear.lineTo(cx + rTeeth * Math.cos(a3), cy + rTeeth * Math.sin(a3));
            gear.lineTo(cx + rRoot * Math.cos(a4), cy + rRoot * Math.sin(a4));
        }
        gear.closePath();

        gear.append(new Ellipse2D.Double(cx - rHole, cy - rHole, rHole * 2, rHole * 2), false);

        g2.setColor(color);
        g2.fill(gear);
        g2.dispose();

        return new ImageIcon(img);
    }

    private static BufferedImage createFallbackLogo(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int pad = Math.max(1, width / 20);
        int rectW = width - (pad * 2);
        int rectH = height - (pad * 2);
        int arc = width / 4;

        // Background Gradient
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(30, 136, 229),
                0, height, new Color(13, 71, 161)
        );
        g2.setPaint(bgGradient);
        g2.fill(new RoundRectangle2D.Float(pad, pad, rectW, rectH, arc, arc));

        // Border Glow
        g2.setColor(new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(Math.max(1f, width / 40f)));
        g2.draw(new RoundRectangle2D.Float(pad, pad, rectW, rectH, arc, arc));

        // Scale factors for inner components based on 100x100 virtual space
        double s = width / 100.0;

        // Java Coffee Cup with Steam (Top Center)
        g2.setColor(new Color(239, 68, 68)); // Red steam
        g2.setStroke(new BasicStroke((float)(2.5 * s)));
        Path2D steam1 = new Path2D.Double();
        steam1.moveTo(46 * s, 26 * s);
        steam1.curveTo(49 * s, 22 * s, 43 * s, 18 * s, 46 * s, 14 * s);
        g2.draw(steam1);

        Path2D steam2 = new Path2D.Double();
        steam2.moveTo(54 * s, 26 * s);
        steam2.curveTo(57 * s, 22 * s, 51 * s, 18 * s, 54 * s, 14 * s);
        g2.draw(steam2);

        // Coffee Cup
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke((float)(2.0 * s)));
        g2.drawArc((int)(40 * s), (int)(25 * s), (int)(20 * s), (int)(10 * s), 180, 180);
        g2.drawArc((int)(36 * s), (int)(32 * s), (int)(28 * s), (int)(6 * s), 0, 360);
        g2.drawArc((int)(57 * s), (int)(26 * s), (int)(7 * s), (int)(6 * s), 270, 180);

        // Orange Capsule (Left)
        Graphics2D gPill = (Graphics2D) g2.create();
        gPill.translate(35 * s, 50 * s);
        gPill.rotate(Math.toRadians(-45));
        gPill.setColor(new Color(249, 115, 22)); // Orange
        gPill.fill(new RoundRectangle2D.Double(-12 * s, -6 * s, 24 * s, 12 * s, 12 * s, 12 * s));
        gPill.setColor(Color.WHITE);
        gPill.fill(new RoundRectangle2D.Double(0, -6 * s, 12 * s, 12 * s, 12 * s, 12 * s));
        gPill.setColor(new Color(194, 65, 12));
        gPill.setFont(Theme.font(Font.BOLD, (int)(7 * s)));
        gPill.drawString("C", (int)(-8 * s), (int)(3 * s));
        gPill.dispose();

        // Yellow Round Pill
        g2.setColor(new Color(250, 204, 21)); // Yellow
        g2.fill(new Ellipse2D.Double(48 * s, 52 * s, 14 * s, 14 * s));
        g2.setColor(new Color(202, 138, 4));
        g2.draw(new Ellipse2D.Double(48 * s, 52 * s, 14 * s, 14 * s));
        g2.draw(new Line2D.Double(51 * s, 59 * s, 59 * s, 59 * s));

        // Medicine Bottle (Right)
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(64 * s, 44 * s, 20 * s, 30 * s, 6 * s, 6 * s));
        g2.setColor(new Color(148, 163, 184)); // Cap
        g2.fill(new RoundRectangle2D.Double(66 * s, 40 * s, 16 * s, 5 * s, 3 * s, 3 * s));
        // Blue label & Cross
        g2.setColor(new Color(14, 165, 233));
        g2.fillRect((int)(64 * s), (int)(54 * s), (int)(20 * s), (int)(12 * s));
        g2.setColor(Color.WHITE);
        g2.fillRect((int)(72 * s), (int)(56 * s), (int)(4 * s), (int)(8 * s));
        g2.fillRect((int)(70 * s), (int)(58 * s), (int)(8 * s), (int)(4 * s));

        // Clipboard Checklist (Bottom Center)
        g2.setColor(new Color(241, 245, 249));
        g2.fill(new RoundRectangle2D.Double(42 * s, 62 * s, 22 * s, 28 * s, 4 * s, 4 * s));
        g2.setColor(new Color(71, 85, 105));
        g2.fill(new RoundRectangle2D.Double(48 * s, 59 * s, 10 * s, 5 * s, 2 * s, 2 * s));
        // Checkmarks
        g2.setColor(new Color(34, 197, 94)); // Green
        g2.setStroke(new BasicStroke((float)(1.5 * s)));
        for (int i = 0; i < 3; i++) {
            double y = (68 + i * 6) * s;
            Path2D check = new Path2D.Double();
            check.moveTo(46 * s, y);
            check.lineTo(48 * s, y + 2 * s);
            check.lineTo(52 * s, y - 2 * s);
            g2.draw(check);
            g2.setColor(new Color(148, 163, 184));
            g2.draw(new Line2D.Double(54 * s, y, 60 * s, y));
            g2.setColor(new Color(34, 197, 94));
        }

        g2.dispose();
        return img;
    }
}
