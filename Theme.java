import java.awt.Color;
import java.awt.Font;

/**
 * Central place for the app's neon dark theme: colors and fonts.
 * Keeping every screen pointed at these constants guarantees a
 * consistent, professional look across the whole application.
 */
public final class Theme {

    private Theme() { }

    // Backgrounds
    public static final Color BG_MAIN     = new Color(13, 15, 26);   // near-black navy (app background)
    public static final Color BG_PANEL    = new Color(20, 23, 38);   // side nav / panels
    public static final Color BG_CARD     = new Color(26, 29, 46);   // cards, tables
    public static final Color BG_LOGIN    = new Color(30, 30, 46);   // #1E1E2E login panel

    // Neon accents
    public static final Color NEON_BLUE   = new Color(0, 191, 255);
    public static final Color NEON_TEAL   = new Color(0, 230, 210);
    public static final Color NEON_GREEN  = new Color(0, 230, 118);  // #00E676
    public static final Color NEON_RED    = new Color(255, 23, 68);  // #FF1744
    public static final Color NEON_PURPLE = new Color(178, 102, 255);
    public static final Color NEON_PINK   = new Color(255, 64, 158);

    // Text
    public static final Color TEXT_WHITE  = new Color(240, 242, 248);
    public static final Color TEXT_MUTED  = new Color(140, 150, 170);

    // Fonts
    public static final String FONT_FAMILY = "Segoe UI";

    public static Font fontBold(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    public static Font fontPlain(int size) {
        return new Font(FONT_FAMILY, Font.PLAIN, size);
    }
}