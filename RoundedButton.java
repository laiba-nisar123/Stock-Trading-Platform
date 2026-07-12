import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A JButton painted as a rounded pill with a solid fill color and
 * a lighter hover state. Used for the side-nav buttons and for the
 * big colorful Buy/Sell/Simulate action buttons.
 */
public class RoundedButton extends JButton {

    private Color baseColor;
    private Color hoverColor;
    private final int arc;
    private boolean active = false;

    public RoundedButton(String text, Color baseColor, Color hoverColor, int arc) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.arc = arc;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(Theme.fontBold(13));

        addChangeListener(e -> repaint());
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill;
        if (active) {
            fill = hoverColor;
        } else if (getModel().isRollover() || getModel().isPressed()) {
            fill = hoverColor;
        } else {
            fill = baseColor;
        }

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
        g2.dispose();

        super.paintComponent(g);
    }
}