import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Screen 1: Login / Welcome Screen.
 * Left side: big attractive market art panel.
 * Right side: dark login form (#1E1E2E) with username, password,
 * and a neon "Enter Trading Desk" button.
 */
public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginScreen() {
        super("Stock Trading Platform - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        add(new MarketArtPanel());
        add(buildLoginPanel());
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_LOGIN);
        panel.setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 50, 0, 50));

        JLabel title = new JLabel("Welcome Back, Trader");
        title.setFont(Theme.fontBold(24));
        title.setForeground(Theme.TEXT_WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Log in to access your trading desk");
        subtitle.setFont(Theme.fontPlain(13));
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userCaption = fieldCaption("USERNAME");
        usernameField = buildTextField();

        JLabel passCaption = fieldCaption("PASSWORD");
        passwordField = buildPasswordField();

        JButton enterBtn = new JButton("Enter Trading Desk  →");
        enterBtn.setFont(Theme.fontBold(14));
        enterBtn.setForeground(Color.BLACK);
        enterBtn.setBackground(Theme.NEON_TEAL);
        enterBtn.setFocusPainted(false);
        enterBtn.setBorder(new EmptyBorder(12, 20, 12, 20));
        enterBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        enterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        enterBtn.setMaximumSize(new Dimension(260, 45));
        enterBtn.addActionListener(e -> attemptLogin());

        JLabel hint = new JLabel("Any username works — this is a risk-free simulation.");
        hint.setFont(Theme.fontPlain(11));
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(35));
        form.add(userCaption);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(20));
        form.add(passCaption);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(30));
        form.add(enterBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(hint);

        panel.add(form);
        return panel;
    }

    private JLabel fieldCaption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.fontBold(11));
        label.setForeground(Theme.NEON_BLUE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField buildTextField() {
        JTextField field = new JTextField();
        field.setFont(Theme.fontPlain(15));
        field.setForeground(Theme.TEXT_WHITE);
        field.setBackground(new Color(40, 40, 58));
        field.setCaretColor(Theme.TEXT_WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.NEON_BLUE),
                new EmptyBorder(8, 10, 8, 10)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(320, 40));
        return field;
    }

    private JPasswordField buildPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(Theme.fontPlain(15));
        field.setForeground(Theme.TEXT_WHITE);
        field.setBackground(new Color(40, 40, 58));
        field.setCaretColor(Theme.TEXT_WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.NEON_BLUE),
                new EmptyBorder(8, 10, 8, 10)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(320, 40));
        return field;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a username to continue.",
                    "Username Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Dashboard dashboard = new Dashboard(username);
        dashboard.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginScreen().setVisible(true);
        });
    }
}