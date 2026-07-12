import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Random;

/**
 * Paints a stylized "trading app" hero visual: dark gradient background,
 * a glowing candlestick-style price line, and subtle grid lines.
 *
 * This exists so the project doesn't depend on an external downloaded
 * image file. In NetBeans this could just as easily be a JLabel with an
 * ImageIcon (Properties -> icon) if you'd rather use a real photo/graphic
 * — swap LoginScreen's left panel for a JLabel if you prefer that route.
 */
public class MarketArtPanel extends JPanel {

    private final double[] points;

    public MarketArtPanel() {
        setOpaque(true);
        Random rnd = new Random(42);
        points = new double[28];
        double value = 0.4;
        for (int i = 0; i < points.length; i++) {
            value += (rnd.nextDouble() - 0.45) * 0.12;
            value = Math.max(0.15, Math.min(0.9, value));
            points[i] = value;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient: deep navy -> near-black
        GradientPaint bg = new GradientPaint(0, 0, new Color(15, 20, 45), 0, h, new Color(8, 10, 20));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // Subtle grid
        g2.setColor(new Color(255, 255, 255, 12));
        for (int gx = 0; gx < w; gx += 40) g2.drawLine(gx, 0, gx, h);
        for (int gy = 0; gy < h; gy += 40) g2.drawLine(0, gy, w, gy);

        // Glowing price line
        Path2D path = new Path2D.Double();
        double stepX = (double) w / (points.length - 1);
        for (int i = 0; i < points.length; i++) {
            double px = i * stepX;
            double py = h - (points[i] * h * 0.7) - h * 0.15;
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }

        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(0, 191, 255, 60));
        g2.draw(path); // glow layer

        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Theme.NEON_TEAL);
        g2.draw(path); // crisp line

        // Branding text
        g2.setFont(Theme.fontBold(30));
        g2.setColor(Theme.TEXT_WHITE);
        g2.drawString("TRADING DESK", 40, 60);

        g2.setFont(Theme.fontPlain(14));
        g2.setColor(Theme.TEXT_MUTED);
        g2.drawString("Simulate. Trade. Learn — with zero real risk.", 40, 85);

        g2.dispose();
    }
}