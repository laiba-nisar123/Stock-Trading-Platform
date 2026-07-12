import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Screen 4: Transaction History — the trader's ledger/passbook.
 * Shown as a floating overlay card on top of the Dashboard.
 * Supports filtering (All / Buys / Sells) and exporting a formatted
 * text report of the trade history to disk.
 */
public class HistoryOverlay extends JPanel {

    private final Dashboard dashboard;
    private final User user;
    private final DataManager dataManager;

    private DefaultTableModel historyModel;
    private JTable historyTable;

    private RoundedButton allBtn;
    private RoundedButton buysBtn;
    private RoundedButton sellsBtn;
    private String currentFilter = "ALL";

    public HistoryOverlay(Dashboard dashboard, User user, DataManager dataManager) {
        this.dashboard = dashboard;
        this.user = user;
        this.dataManager = dataManager;

        setOpaque(false);
        setLayout(new BorderLayout(0, 12));

        add(buildFilterRow(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildExportRow(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);

        allBtn = filterButton("All");
        buysBtn = filterButton("Buys");
        sellsBtn = filterButton("Sells");

        allBtn.addActionListener(e -> applyFilter("ALL"));
        buysBtn.addActionListener(e -> applyFilter("BUY"));
        sellsBtn.addActionListener(e -> applyFilter("SELL"));

        row.add(allBtn);
        row.add(buysBtn);
        row.add(sellsBtn);
        applyFilter("ALL");
        return row;
    }

    private RoundedButton filterButton(String label) {
        RoundedButton btn = new RoundedButton(label, Theme.BG_PANEL, new Color(45, 50, 75), 12);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        return btn;
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        allBtn.setActive(filter.equals("ALL"));
        buysBtn.setActive(filter.equals("BUY"));
        sellsBtn.setActive(filter.equals("SELL"));
        refreshTable();
    }

    private JScrollPane buildTable() {
        historyModel = new DefaultTableModel(
                new Object[]{"Date", "Action", "Symbol", "Quantity", "Price ($)", "Total ($)"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(30);
        historyTable.setFont(Theme.fontPlain(13));
        historyTable.setForeground(Theme.TEXT_WHITE);
        historyTable.setBackground(Theme.BG_CARD);
        historyTable.setGridColor(new Color(50, 55, 75));
        historyTable.setSelectionBackground(new Color(45, 55, 85));
        historyTable.setShowVerticalLines(false);
        historyTable.setIntercellSpacing(new Dimension(0, 1));

        historyTable.getTableHeader().setFont(Theme.fontBold(12));
        historyTable.getTableHeader().setBackground(new Color(35, 38, 58));
        historyTable.getTableHeader().setForeground(Theme.NEON_TEAL);
        historyTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                c.setBackground(sel ? new Color(45, 55, 85) : (row % 2 == 0 ? Theme.BG_CARD : new Color(22, 25, 40)));
                c.setForeground(Theme.TEXT_WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        historyTable.getColumnModel().getColumn(1).setCellRenderer(new ActionTagRenderer());

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 55, 75), 1));
        scroll.getViewport().setBackground(Theme.BG_CARD);
        return scroll;
    }

    /** Colors the BUY/SELL tag: green pill for BUY, red pill for SELL. */
    private class ActionTagRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            String text = v == null ? "" : v.toString();
            setFont(Theme.fontBold(12));
            setHorizontalAlignment(SwingConstants.CENTER);
            c.setBackground(sel ? new Color(45, 55, 85) : (row % 2 == 0 ? Theme.BG_CARD : new Color(22, 25, 40)));
            setForeground(text.equals("BUY") ? Theme.NEON_GREEN : Theme.NEON_RED);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    private JPanel buildExportRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        RoundedButton exportBtn = new RoundedButton("⬇  Export History (.txt)", Theme.NEON_BLUE, new Color(0, 150, 210), 14);
        exportBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        exportBtn.addActionListener(e -> exportHistory());
        row.add(exportBtn);
        return row;
    }

    private List<Transaction> filteredTransactions() {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : user.getHistory()) {
            if (currentFilter.equals("ALL") || tx.getType().name().equals(currentFilter)) {
                result.add(tx);
            }
        }
        return result;
    }

    private void refreshTable() {
        historyModel.setRowCount(0);
        List<Transaction> list = filteredTransactions();
        // Show most recent first
        for (int i = list.size() - 1; i >= 0; i--) {
            Transaction tx = list.get(i);
            historyModel.addRow(new Object[]{
                    tx.getFormattedDate(), tx.getType().name(), tx.getSymbol(), tx.getQuantity(),
                    String.format("%,.2f", tx.getPriceAtTransaction()),
                    String.format("%,.2f", tx.getTotalValue())
            });
        }

        if (list.isEmpty()) {
            historyModel.addRow(new Object[]{"—", "—", "No transactions yet", "—", "—", "—"});
        }
    }

    private void exportHistory() {
        File file = dataManager.exportHistoryReport(user, filteredTransactions());
        if (file != null) {
            JOptionPane.showMessageDialog(this,
                    "History exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Something went wrong while exporting your history.",
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}