import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single buy or sell transaction performed by a user.
 * Used to build the transaction history ledger.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public enum Type { BUY, SELL }

    private Type type;
    private String symbol;
    private int quantity;
    private double priceAtTransaction;
    private LocalDateTime timestamp;

    /** Creates a brand-new transaction, stamped with the current time. */
    public Transaction(Type type, String symbol, int quantity, double priceAtTransaction) {
        this(type, symbol, quantity, priceAtTransaction, LocalDateTime.now());
    }

    /** Rebuilds a transaction with a specific (previously stored) timestamp. */
    public Transaction(Type type, String symbol, int quantity, double priceAtTransaction, LocalDateTime timestamp) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.priceAtTransaction = priceAtTransaction;
        this.timestamp = timestamp;
    }

    public double getTotalValue() {
        return quantity * priceAtTransaction;
    }

    public Type getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPriceAtTransaction() {
        return priceAtTransaction;
    }

    public String getFormattedTime() {
        return timestamp.format(FULL_FMT);
    }

    public String getFormattedDate() {
        return timestamp.format(DATE_FMT);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-4s %4d x %-6s @ $%,9.2f = $%,10.2f",
                getFormattedTime(), type, quantity, symbol, priceAtTransaction, getTotalValue());
    }

    /** Serializes this transaction into a single CSV line for file storage. */
    public String toCsvLine() {
        return type + "," + symbol + "," + quantity + "," + priceAtTransaction + "," + getFormattedTime();
    }

    /** Rebuilds a Transaction object from a stored CSV line, preserving its original timestamp. */
    public static Transaction fromCsvLine(String line) {
        String[] parts = line.split(",");
        Type t = Type.valueOf(parts[0]);
        String symbol = parts[1];
        int qty = Integer.parseInt(parts[2]);
        double price = Double.parseDouble(parts[3]);
        LocalDateTime ts;
        try {
            ts = LocalDateTime.parse(parts[4], FULL_FMT);
        } catch (Exception e) {
            ts = LocalDateTime.now();
        }
        return new Transaction(t, symbol, qty, price, ts);
    }
}