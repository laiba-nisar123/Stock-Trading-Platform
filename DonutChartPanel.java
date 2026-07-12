import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Draws a donut chart representing how the trader's total net worth is
 * split between cash and each stock currently held. Slice colors cycle
 * through the neon palette so it matches the rest of the theme.
 */
public class DonutChartPanel extends JPanel {

    private final Color[] palette = {
            Theme.NEON_BLUE, Theme.NEON_PURPLE, Theme.NEON_GREEN,
            Theme.NEON_PINK, Theme.NEON_TEAL, Theme.NEON_RED
    };

    private Map<String, Double> slices = new LinkedHashMap<>();

    public DonutChartPanel() {
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(180, 180));
    }

    /** Updates the chart data. Keys are labels (e.g. "AAPL", "Cash"), values are dollar amounts. */
    public void setData(Map<String, Double> newSlices) {
        this.slices = newSlices;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double total = slices.values().stream().mapToDouble(Double::doubleValue).sum();
        int size = Math.min(getWidth(), getHeight()) - 10;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        if (total <= 0 || slices.isEmpty()) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.fontPlain(12));
            g2.drawString("No holdings yet", 20, getHeight() / 2);
            g2.dispose();
            return;
        }

        double startAngle = 90;
        int colorIndex = 0;
        int thickness = 26;

        for (Map.Entry<String, Double> entry : slices.entrySet()) {
            double sweep = (entry.getValue() / total) * 360.0;
            g2.setColor(palette[colorIndex % palette.length]);
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            Arc2D arc = new Arc2D.Double(x + thickness / 2.0, y + thickness / 2.0,
                    size - thickness, size - thickness, startAngle, -sweep, Arc2D.OPEN);
            g2.draw(arc);
            startAngle -= sweep;
            colorIndex++;
        }

        g2.dispose();
    }

    /** Builds a small legend panel to accompany the chart, matching slice colors. */
    public JPanel buildLegend() {
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new javax.swing.BoxLayout(legend, javax.swing.BoxLayout.Y_AXIS));

        int colorIndex = 0;
        for (String label : slices.keySet()) {
            JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));
            row.setOpaque(false);

            JPanel dot = new JPanel();
            dot.setPreferredSize(new java.awt.Dimension(10, 10));
            dot.setBackground(palette[colorIndex % palette.length]);

            javax.swing.JLabel text = new javax.swing.JLabel(label);
            text.setForeground(Theme.TEXT_WHITE);
            text.setFont(Theme.fontPlain(11));

            row.add(dot);
            row.add(text);
            legend.add(row);
            colorIndex++;
        }
        return legend;
    }
}