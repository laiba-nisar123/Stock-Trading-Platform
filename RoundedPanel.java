import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A JPanel with rounded corners, used everywhere for the "card" look
 * (balance card, floating modals, table containers, nav pills).
 * Supports either a flat fill color or a diagonal two-color gradient.
 */
public class RoundedPanel extends JPanel {

    private final int arc;
    private Color fillColor;
    private Color gradientStart;
    private Color gradientEnd;
    private Color borderColor;
    private int borderThickness = 0;

    public RoundedPanel(int arc, Color fillColor) {
        this.arc = arc;
        this.fillColor = fillColor;
        setOpaque(false);
    }

    public static RoundedPanel gradient(int arc, Color start, Color end) {
        RoundedPanel p = new RoundedPanel(arc, start);
        p.gradientStart = start;
        p.gradientEnd = end;
        return p;
    }

    public void setBorderGlow(Color color, int thickness) {
        this.borderColor = color;
        this.borderThickness = thickness;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);

        if (gradientStart != null && gradientEnd != null) {
            GradientPaint gp = new GradientPaint(0, 0, gradientStart, getWidth(), getHeight(), gradientEnd);
            g2.setPaint(gp);
        } else {
            g2.setColor(fillColor);
        }
        g2.fill(shape);

        if (borderColor != null && borderThickness > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(borderThickness));
            g2.draw(new RoundRectangle2D.Float(
                    borderThickness / 2f, borderThickness / 2f,
                    getWidth() - borderThickness, getHeight() - borderThickness, arc, arc));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}