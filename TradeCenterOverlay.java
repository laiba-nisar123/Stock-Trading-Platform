import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Optional;
import java.util.Random;

/**
 * Screen 3: Trade Center — the interactive Buy/Sell panel.
 * Shown as a floating overlay card on top of the Dashboard.
 */
public class TradeCenterOverlay extends JPanel {

    private final Dashboard dashboard;
    private final StockMarket market;
    private final User user;

    private boolean buyMode = true;

    private RoundedButton buyToggle;
    private RoundedButton sellToggle;
    private JComboBox<String> stockCombo;
    private JTextField quantityField;
    private JLabel pricePerShareValue;
    private JLabel totalValue;
    private JPanel orderBookPanel;

    public TradeCenterOverlay(Dashboard dashboard, StockMarket market, User user) {
        this.dashboard = dashboard;
        this.market = market;
        this.user = user;

        setOpaque(false);
        setLayout(new BorderLayout(16, 10));

        JLabel subtitle = new JLabel("Choose a stock and simulate your trade below.");
        subtitle.setFont(Theme.fontPlain(12));
        subtitle.setForeground(Theme.TEXT_MUTED);
        add(subtitle, BorderLayout.NORTH);

        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildOrderBookPanel(), BorderLayout.EAST);

        refreshComputedLabels();
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JPanel toggleRow = new JPanel(new GridLayout(1, 2, 12, 0));
        toggleRow.setOpaque(false);
        toggleRow.setMaximumSize(new Dimension(340, 55));
        toggleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        buyToggle = new RoundedButton("BUY", Theme.NEON_GREEN, new Color(0, 190, 95), 14);
        sellToggle = new RoundedButton("SELL", new Color(60, 30, 40), Theme.NEON_RED, 14);
        buyToggle.setForeground(Color.BLACK);
        buyToggle.setPreferredSize(new Dimension(140, 50));
        sellToggle.setPreferredSize(new Dimension(140, 50));
        buyToggle.setActive(true);

        buyToggle.addActionListener(e -> {
            buyMode = true;
            buyToggle.setActive(true);
            sellToggle.setActive(false);
        });
        sellToggle.addActionListener(e -> {
            buyMode = false;
            sellToggle.setActive(true);
            buyToggle.setActive(false);
        });

        toggleRow.add(buyToggle);
        toggleRow.add(sellToggle);

        JLabel comboCaption = sectionCaption("STOCK");
        stockCombo = new JComboBox<>();
        for (Stock s : market.getAllStocks()) {
            stockCombo.addItem(s.getSymbol() + " — " + s.getName());
        }
        stockCombo.setFont(Theme.fontPlain(13));
        stockCombo.setBackground(new Color(38, 42, 62));
        stockCombo.setForeground(Theme.TEXT_WHITE);
        stockCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        stockCombo.setMaximumSize(new Dimension(340, 38));
        stockCombo.addActionListener(e -> refreshComputedLabels());

        JLabel qtyCaption = sectionCaption("QUANTITY");
        quantityField = new JTextField("1");
        quantityField.setFont(Theme.fontPlain(14));
        quantityField.setForeground(Theme.TEXT_WHITE);
        quantityField.setBackground(new Color(38, 42, 62));
        quantityField.setCaretColor(Theme.TEXT_WHITE);
        quantityField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.NEON_BLUE),
                new EmptyBorder(6, 10, 6, 10)));
        quantityField.setAlignmentX(Component.LEFT_ALIGNMENT);
        quantityField.setMaximumSize(new Dimension(340, 38));
        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshComputedLabels(); }
            public void removeUpdate(DocumentEvent e) { refreshComputedLabels(); }
            public void changedUpdate(DocumentEvent e) { refreshComputedLabels(); }
        });

        JPanel infoRow = new JPanel(new GridLayout(1, 2, 20, 0));
        infoRow.setOpaque(false);
        infoRow.setMaximumSize(new Dimension(340, 55));
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel priceBox = new JPanel(new GridLayout(2, 1));
        priceBox.setOpaque(false);
        JLabel priceCap = sectionCaption("PRICE / SHARE");
        pricePerShareValue = new JLabel("$0.00");
        pricePerShareValue.setFont(Theme.fontBold(18));
        pricePerShareValue.setForeground(Theme.NEON_TEAL);
        priceBox.add(priceCap);
        priceBox.add(pricePerShareValue);

        JPanel totalBox = new JPanel(new GridLayout(2, 1));
        totalBox.setOpaque(false);
        JLabel totalCap = sectionCaption("TOTAL");
        totalValue = new JLabel("$0.00");
        totalValue.setFont(Theme.fontBold(18));
        totalValue.setForeground(Theme.TEXT_WHITE);
        totalBox.add(totalCap);
        totalBox.add(totalValue);

        infoRow.add(priceBox);
        infoRow.add(totalBox);

        RoundedButton simulateBtn = new RoundedButton("⚡  Simulate Transaction", Theme.NEON_BLUE, new Color(0, 150, 210), 14);
        simulateBtn.setForeground(Color.WHITE);
        simulateBtn.setBorder(new EmptyBorder(12, 20, 12, 20));
        simulateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        simulateBtn.setMaximumSize(new Dimension(340, 46));
        simulateBtn.addActionListener(e -> executeTransaction());

        form.add(toggleRow);
        form.add(Box.createVerticalStrut(18));
        form.add(comboCaption);
        form.add(Box.createVerticalStrut(4));
        form.add(stockCombo);
        form.add(Box.createVerticalStrut(14));
        form.add(qtyCaption);
        form.add(Box.createVerticalStrut(4));
        form.add(quantityField);
        form.add(Box.createVerticalStrut(16));
        form.add(infoRow);
        form.add(Box.createVerticalStrut(20));
        form.add(simulateBtn);

        return form;
    }

    private JLabel sectionCaption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.fontBold(10));
        label.setForeground(Theme.NEON_BLUE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel buildOrderBookPanel() {
        RoundedPanel panel = new RoundedPanel(14, new Color(18, 21, 34));
        panel.setPreferredSize(new Dimension(190, 0));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel title = new JLabel("📖 Order Book");
        title.setFont(Theme.fontBold(12));
        title.setForeground(Theme.TEXT_WHITE);
        panel.add(title, BorderLayout.NORTH);

        orderBookPanel = new JPanel();
        orderBookPanel.setOpaque(false);
        orderBookPanel.setLayout(new BoxLayout(orderBookPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(orderBookPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshOrderBook(Stock stock) {
        orderBookPanel.removeAll();
        Random rnd = new Random(stock.getSymbol().hashCode() + System.currentTimeMillis() / 100000);

        orderBookPanel.add(orderBookRow("SELL", Theme.NEON_RED, true));
        for (int i = 5; i >= 1; i--) {
            double p = stock.getPrice() + (stock.getPrice() * 0.002 * i);
            int vol = 1 + rnd.nextInt(9);
            orderBookPanel.add(priceRow(p, vol, Theme.NEON_RED));
        }
        orderBookPanel.add(Box.createVerticalStrut(6));
        orderBookPanel.add(orderBookRow("BUY", Theme.NEON_GREEN, true));
        for (int i = 1; i <= 5; i++) {
            double p = stock.getPrice() - (stock.getPrice() * 0.002 * i);
            int vol = 1 + rnd.nextInt(9);
            orderBookPanel.add(priceRow(p, vol, Theme.NEON_GREEN));
        }

        orderBookPanel.revalidate();
        orderBookPanel.repaint();
    }

    private JLabel orderBookRow(String text, Color color, boolean header) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.fontBold(11));
        label.setForeground(color);
        label.setBorder(new EmptyBorder(4, 2, 4, 2));
        return label;
    }

    private JPanel priceRow(double price, int volume, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel priceLabel = new JLabel(String.format("%.2f", price));
        priceLabel.setFont(Theme.fontPlain(11));
        priceLabel.setForeground(color);
        JLabel volLabel = new JLabel(String.valueOf(volume));
        volLabel.setFont(Theme.fontPlain(11));
        volLabel.setForeground(Theme.TEXT_MUTED);
        row.add(priceLabel, BorderLayout.WEST);
        row.add(volLabel, BorderLayout.EAST);
        return row;
    }

    private Optional<Stock> getSelectedStock() {
        int idx = stockCombo.getSelectedIndex();
        if (idx < 0) return Optional.empty();
        return Optional.of(market.getAllStocks().get(idx));
    }

    private void refreshComputedLabels() {
        Optional<Stock> stockOpt = getSelectedStock();
        if (stockOpt.isEmpty()) return;
        Stock stock = stockOpt.get();

        pricePerShareValue.setText(String.format("$%,.2f", stock.getPrice()));

        int qty = parseQuantity();
        double total = qty * stock.getPrice();
        totalValue.setText(String.format("$%,.2f", total));

        refreshOrderBook(stock);
    }

    private int parseQuantity() {
        try {
            return Math.max(0, Integer.parseInt(quantityField.getText().trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void executeTransaction() {
        Optional<Stock> stockOpt = getSelectedStock();
        if (stockOpt.isEmpty()) return;
        Stock stock = stockOpt.get();

        int quantity = parseQuantity();
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity greater than zero.",
                    "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (buyMode) {
            double cost = stock.getPrice() * quantity;
            if (cost > user.getBalance()) {
                JOptionPane.showMessageDialog(this,
                        String.format("Insufficient balance.\nRequired: $%,.2f\nAvailable: $%,.2f", cost, user.getBalance()),
                        "Insufficient Funds", JOptionPane.ERROR_MESSAGE);
                return;
            }
            user.buyStock(stock, quantity);
            JOptionPane.showMessageDialog(this,
                    String.format("✔ Bought %d share(s) of %s for $%,.2f.", quantity, stock.getSymbol(), cost),
                    "Transaction Successful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            int owned = user.getQuantityOwned(stock.getSymbol());
            if (quantity > owned) {
                JOptionPane.showMessageDialog(this,
                        "You only own " + owned + " share(s) of " + stock.getSymbol() + ".",
                        "Insufficient Shares", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double proceeds = stock.getPrice() * quantity;
            user.sellStock(stock, quantity);
            JOptionPane.showMessageDialog(this,
                    String.format("✔ Sold %d share(s) of %s for $%,.2f.", quantity, stock.getSymbol(), proceeds),
                    "Transaction Successful", JOptionPane.INFORMATION_MESSAGE);
        }

        dashboard.refreshAll();
        refreshComputedLabels();
    }
}