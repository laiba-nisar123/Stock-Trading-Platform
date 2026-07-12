import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Screen 2: Main Market Dashboard.
 * Persistent chrome: side nav (pill buttons), balance card, profile card,
 * and a donut chart of portfolio allocation. The center area swaps between
 * the Market Overview table and the My Portfolio table via CardLayout.
 * Trade Center and Transaction History open as floating rounded "modal"
 * cards over a dimmed glass pane (Screens 3 & 4).
 */
public class Dashboard extends JFrame {

    private static final double STARTING_BALANCE = 10000.00;

    private final StockMarket market = new StockMarket();
    private final DataManager dataManager = new DataManager();
    private User currentUser;

    // Top chrome
    private JLabel balanceValueLabel;
    private JLabel profileNameLabel;
    private DonutChartPanel donutChart;
    private JPanel donutCardContainer;

    // Center content
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private DefaultTableModel marketModel;
    private DefaultTableModel portfolioModel;
    private JLabel totalValueLabel;
    private JLabel totalChangeLabel;

    // Nav
    private RoundedButton navMarketBtn;
    private RoundedButton navTradeBtn;
    private RoundedButton navPortfolioBtn;
    private RoundedButton navHistoryBtn;
    private RoundedButton navLogoutBtn;

    // Overlay (glass pane)
    private JPanel glassPane;

    public Dashboard(String username) {
        super("Stock Trading Platform - Dashboard");

        User loaded = dataManager.loadUser(username, STARTING_BALANCE);
        currentUser = (loaded != null) ? loaded : new User(username, STARTING_BALANCE);
        market.simulateMarketMovement();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAndExit();
            }
        });

        setSize(1150, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        setupGlassPane();

        add(buildSideNav(), BorderLayout.WEST);
        add(buildTopAndCenter(), BorderLayout.CENTER);

        refreshAll();
        setActiveNav(navMarketBtn);
    }

    // ---------------------------------------------------------------
    // Layout: Side Navigation
    // ---------------------------------------------------------------

    private JPanel buildSideNav() {
        RoundedPanel nav = new RoundedPanel(0, Theme.BG_PANEL);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(210, 0));
        nav.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel logo = new JLabel("📈 TRADING DESK");
        logo.setFont(Theme.fontBold(15));
        logo.setForeground(Theme.NEON_TEAL);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(logo);
        nav.add(Box.createVerticalStrut(35));

        navMarketBtn = navButton("📊  Market Overview");
        navTradeBtn = navButton("🔁  Trade Center");
        navPortfolioBtn = navButton("💼  My Portfolio");
        navHistoryBtn = navButton("🧾  History");
        navLogoutBtn = navButton("🚪  Logout");

        navMarketBtn.addActionListener(e -> {
            contentCardLayout.show(contentPanel, "MARKET");
            setActiveNav(navMarketBtn);
        });
        navPortfolioBtn.addActionListener(e -> {
            refreshPortfolioTable();
            contentCardLayout.show(contentPanel, "PORTFOLIO");
            setActiveNav(navPortfolioBtn);
        });
        navTradeBtn.addActionListener(e -> {
            setActiveNav(navTradeBtn);
            openTradeCenter();
        });
        navHistoryBtn.addActionListener(e -> {
            setActiveNav(navHistoryBtn);
            openHistory();
        });
        navLogoutBtn.addActionListener(e -> logout());

        nav.add(navMarketBtn);
        nav.add(Box.createVerticalStrut(10));
        nav.add(navTradeBtn);
        nav.add(Box.createVerticalStrut(10));
        nav.add(navPortfolioBtn);
        nav.add(Box.createVerticalStrut(10));
        nav.add(navHistoryBtn);
        nav.add(Box.createVerticalGlue());
        nav.add(navLogoutBtn);

        return nav;
    }

    private RoundedButton navButton(String text) {
        RoundedButton btn = new RoundedButton(text, Theme.BG_CARD, new Color(45, 50, 75), 14);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 46));
        return btn;
    }

    private void setActiveNav(RoundedButton active) {
        for (RoundedButton b : new RoundedButton[]{navMarketBtn, navTradeBtn, navPortfolioBtn, navHistoryBtn}) {
            b.setActive(b == active);
        }
    }

    // ---------------------------------------------------------------
    // Layout: Top chrome + center content
    // ---------------------------------------------------------------

    private JPanel buildTopAndCenter() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_MAIN);
        wrapper.add(buildTopBar(), BorderLayout.NORTH);
        wrapper.add(buildCenterCards(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new GridLayout(1, 3, 18, 0));
        top.setBackground(Theme.BG_MAIN);
        top.setBorder(new EmptyBorder(22, 22, 10, 22));

        // Balance card (gradient cyan -> purple)
        RoundedPanel balanceCard = RoundedPanel.gradient(20, new Color(0, 172, 193), new Color(142, 68, 224));
        balanceCard.setLayout(new BoxLayout(balanceCard, BoxLayout.Y_AXIS));
        balanceCard.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel balCaption = new JLabel("AVAILABLE FAKE BALANCE");
        balCaption.setFont(Theme.fontBold(11));
        balCaption.setForeground(new Color(255, 255, 255, 210));
        balanceValueLabel = new JLabel("$0.00");
        balanceValueLabel.setFont(Theme.fontBold(28));
        balanceValueLabel.setForeground(Color.WHITE);
        balanceCard.add(balCaption);
        balanceCard.add(Box.createVerticalStrut(6));
        balanceCard.add(balanceValueLabel);

        // Profile card
        RoundedPanel profileCard = new RoundedPanel(20, Theme.BG_CARD);
        profileCard.setLayout(new BorderLayout());
        profileCard.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel avatar = new JLabel("🧑‍💼");
        avatar.setFont(Theme.fontPlain(34));
        JPanel profileText = new JPanel(new GridLayout(2, 1));
        profileText.setOpaque(false);
        JLabel caption = new JLabel("PROFILE");
        caption.setFont(Theme.fontBold(10));
        caption.setForeground(Theme.TEXT_MUTED);
        profileNameLabel = new JLabel();
        profileNameLabel.setFont(Theme.fontBold(16));
        profileNameLabel.setForeground(Theme.TEXT_WHITE);
        profileText.add(caption);
        profileText.add(profileNameLabel);
        profileCard.add(avatar, BorderLayout.WEST);
        profileCard.add(profileText, BorderLayout.CENTER);

        // Donut chart card (portfolio allocation)
        donutCardContainer = new RoundedPanel(20, Theme.BG_CARD);
        donutCardContainer.setLayout(new BorderLayout(10, 0));
        donutCardContainer.setBorder(new EmptyBorder(10, 14, 10, 14));
        donutChart = new DonutChartPanel();
        donutCardContainer.add(donutChart, BorderLayout.WEST);

        top.add(balanceCard);
        top.add(profileCard);
        top.add(donutCardContainer);
        return top;
    }

    private JPanel buildCenterCards() {
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Theme.BG_MAIN);
        contentPanel.setBorder(new EmptyBorder(10, 22, 22, 22));

        contentPanel.add(buildMarketOverviewCard(), "MARKET");
        contentPanel.add(buildPortfolioCard(), "PORTFOLIO");
        return contentPanel;
    }

    private JPanel buildMarketOverviewCard() {
        RoundedPanel card = new RoundedPanel(18, Theme.BG_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel heading = new JLabel("📊 Market Overview — Sample Stocks");
        heading.setFont(Theme.fontBold(16));
        heading.setForeground(Theme.TEXT_WHITE);
        card.add(heading, BorderLayout.NORTH);

        marketModel = new DefaultTableModel(new Object[]{"Symbol", "Company", "Price ($)", "Change"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(marketModel);
        styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new ChangeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setOpaque(false);

        JPanel scrollWrap = new JPanel(new BorderLayout());
        scrollWrap.setOpaque(false);
        scrollWrap.setBorder(new EmptyBorder(14, 0, 10, 0));
        scrollWrap.add(scroll, BorderLayout.CENTER);
        card.add(scrollWrap, BorderLayout.CENTER);

        JButton refreshBtn = new RoundedButton("🔄  Refresh Prices", Theme.NEON_BLUE, new Color(0, 150, 210), 12);
        refreshBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        refreshBtn.addActionListener(e -> {
            market.simulateMarketMovement();
            refreshAll();
        });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.add(refreshBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildPortfolioCard() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);

        RoundedPanel totalCard = RoundedPanel.gradient(18, new Color(10, 30, 30), new Color(20, 55, 50));
        totalCard.setBorderGlow(Theme.NEON_GREEN, 2);
        totalCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 14));
        JLabel rocket = new JLabel("🚀");
        rocket.setFont(Theme.fontPlain(30));
        JPanel valueBox = new JPanel(new GridLayout(2, 1));
        valueBox.setOpaque(false);
        JLabel cap = new JLabel("TOTAL PORTFOLIO VALUE");
        cap.setFont(Theme.fontBold(11));
        cap.setForeground(Theme.TEXT_MUTED);
        totalValueLabel = new JLabel("$0.00");
        totalValueLabel.setFont(Theme.fontBold(24));
        totalValueLabel.setForeground(Theme.TEXT_WHITE);
        valueBox.add(cap);
        valueBox.add(totalValueLabel);
        totalChangeLabel = new JLabel("+0.0%");
        totalChangeLabel.setFont(Theme.fontBold(20));
        totalCard.add(rocket);
        totalCard.add(valueBox);
        totalCard.add(totalChangeLabel);

        RoundedPanel tableCard = new RoundedPanel(18, Theme.BG_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel heading = new JLabel("💼 My Portfolio");
        heading.setFont(Theme.fontBold(16));
        heading.setForeground(Theme.TEXT_WHITE);
        tableCard.add(heading, BorderLayout.NORTH);

        portfolioModel = new DefaultTableModel(
                new Object[]{"Symbol", "Company", "Shares", "Current Value ($)", "Profit/Loss"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(portfolioModel);
        styleTable(table);
        table.getColumnModel().getColumn(4).setCellRenderer(new ChangeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_CARD);

        JPanel scrollWrap = new JPanel(new BorderLayout());
        scrollWrap.setOpaque(false);
        scrollWrap.setBorder(new EmptyBorder(14, 0, 0, 0));
        scrollWrap.add(scroll, BorderLayout.CENTER);
        tableCard.add(scrollWrap, BorderLayout.CENTER);

        wrapper.add(totalCard, BorderLayout.NORTH);
        wrapper.add(tableCard, BorderLayout.CENTER);
        return wrapper;
    }

    // ---------------------------------------------------------------
    // Table styling + colored renderers
    // ---------------------------------------------------------------

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(Theme.fontPlain(13));
        table.setForeground(Theme.TEXT_WHITE);
        table.setBackground(Theme.BG_CARD);
        table.setGridColor(new Color(50, 55, 75));
        table.setSelectionBackground(new Color(45, 55, 85));
        table.setSelectionForeground(Theme.TEXT_WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.getTableHeader().setFont(Theme.fontBold(12));
        table.getTableHeader().setBackground(new Color(35, 38, 58));
        table.getTableHeader().setForeground(Theme.NEON_TEAL);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                c.setBackground(sel ? new Color(45, 55, 85) : (row % 2 == 0 ? Theme.BG_CARD : new Color(22, 25, 40)));
                c.setForeground(Theme.TEXT_WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    /** Colors +/- change or profit/loss text green or red. */
    private class ChangeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            String text = v == null ? "" : v.toString();
            setFont(Theme.fontBold(13));
            c.setBackground(sel ? new Color(45, 55, 85) : (row % 2 == 0 ? Theme.BG_CARD : new Color(22, 25, 40)));
            if (text.startsWith("+") || text.startsWith("▲")) {
                setForeground(Theme.NEON_GREEN);
            } else if (text.startsWith("-") || text.startsWith("▼")) {
                setForeground(Theme.NEON_RED);
            } else {
                setForeground(Theme.TEXT_MUTED);
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    // ---------------------------------------------------------------
    // Overlay (floating modal) system
    // ---------------------------------------------------------------

    private void setupGlassPane() {
        glassPane = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 165));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false);
        glassPane.addMouseListener(new MouseAdapter() { }); // swallow clicks behind overlay
        setGlassPane(glassPane);
    }

    public void showOverlay(JComponent card) {
        glassPane.removeAll();
        glassPane.add(card);
        glassPane.setVisible(true);
        glassPane.revalidate();
        glassPane.repaint();
    }

    public void hideOverlay() {
        glassPane.setVisible(false);
    }

    /** Builds a floating rounded card with a title bar and close (✕) button. */
    public RoundedPanel wrapAsFloatingCard(String title, JComponent content, int width, int height) {
        RoundedPanel card = new RoundedPanel(22, Theme.BG_CARD);
        card.setBorderGlow(Theme.NEON_BLUE, 2);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(width, height));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 10, 16));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.fontBold(16));
        titleLabel.setForeground(Theme.TEXT_WHITE);

        RoundedButton closeBtn = new RoundedButton("✕", Theme.BG_PANEL, Theme.NEON_RED, 10);
        closeBtn.setPreferredSize(new Dimension(30, 30));
        closeBtn.addActionListener(e -> hideOverlay());

        header.add(titleLabel, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.setBorder(new EmptyBorder(0, 20, 20, 20));
        contentWrap.add(content, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(contentWrap, BorderLayout.CENTER);
        return card;
    }

    private void openTradeCenter() {
        TradeCenterOverlay overlay = new TradeCenterOverlay(this, market, currentUser);
        RoundedPanel card = wrapAsFloatingCard("🔁 TRADE CENTER", overlay, 620, 460);
        showOverlay(card);
    }

    private void openHistory() {
        HistoryOverlay overlay = new HistoryOverlay(this, currentUser, dataManager);
        RoundedPanel card = wrapAsFloatingCard("🧾 TRANSACTION HISTORY", overlay, 680, 480);
        showOverlay(card);
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Save your data and log out?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dataManager.saveUser(currentUser);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
        }
    }

    private void saveAndExit() {
        dataManager.saveUser(currentUser);
        JOptionPane.showMessageDialog(this,
                "Your data has been saved. Happy trading, " + currentUser.getUsername() + "!",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        System.exit(0);
    }

    // ---------------------------------------------------------------
    // Refresh / Rendering
    // ---------------------------------------------------------------

    public void refreshAll() {
        profileNameLabel.setText(currentUser.getUsername());
        balanceValueLabel.setText(String.format("$%,.2f", currentUser.getBalance()));

        refreshMarketTable();
        refreshPortfolioTable();
        refreshDonutChart();
    }

    private void refreshMarketTable() {
        marketModel.setRowCount(0);
        for (Stock s : market.getAllStocks()) {
            double changePct = s.getChangePercent();
            String changeText = changePct > 0.001 ? String.format("+%.2f%%", changePct)
                    : changePct < -0.001 ? String.format("%.2f%%", changePct)
                    : "0.00%";
            marketModel.addRow(new Object[]{
                    s.getSymbol(), s.getName(), String.format("%,.2f", s.getPrice()), changeText
            });
        }
    }

    private void refreshPortfolioTable() {
        portfolioModel.setRowCount(0);
        double totalValue = currentUser.getBalance();

        for (Map.Entry<String, Integer> entry : currentUser.getPortfolio().entrySet()) {
            Optional<Stock> stockOpt = market.findStock(entry.getKey());
            if (stockOpt.isPresent()) {
                Stock s = stockOpt.get();
                double value = s.getPrice() * entry.getValue();
                totalValue += value;
                double plPct = currentUser.getProfitLossPercent(entry.getKey(), s.getPrice());
                String plText = plPct >= 0 ? String.format("+%.2f%%", plPct) : String.format("%.2f%%", plPct);

                portfolioModel.addRow(new Object[]{
                        entry.getKey(), s.getName(), entry.getValue(),
                        String.format("%,.2f", value), plText
                });
            }
        }

        totalValueLabel.setText(String.format("$%,.2f", totalValue));
        double overallChangePct = ((totalValue - STARTING_BALANCE) / STARTING_BALANCE) * 100.0;
        totalChangeLabel.setText((overallChangePct >= 0 ? "+" : "") + String.format("%.1f%%", overallChangePct));
        totalChangeLabel.setForeground(overallChangePct >= 0 ? Theme.NEON_GREEN : Theme.NEON_RED);
    }

    private void refreshDonutChart() {
        Map<String, Double> slices = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : currentUser.getPortfolio().entrySet()) {
            Optional<Stock> stockOpt = market.findStock(entry.getKey());
            stockOpt.ifPresent(s -> slices.put(entry.getKey(), s.getPrice() * entry.getValue()));
        }
        slices.put("Cash", currentUser.getBalance());
        donutChart.setData(slices);

        donutCardContainer.removeAll();
        donutCardContainer.setLayout(new BorderLayout(10, 0));
        donutCardContainer.add(donutChart, BorderLayout.WEST);
        donutCardContainer.add(donutChart.buildLegend(), BorderLayout.CENTER);
        donutCardContainer.revalidate();
        donutCardContainer.repaint();
    }

    // ---------------------------------------------------------------
    // Package-private accessors for overlay panels
    // ---------------------------------------------------------------

    StockMarket getMarket() { return market; }
    User getCurrentUser() { return currentUser; }
}