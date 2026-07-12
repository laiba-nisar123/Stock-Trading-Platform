import java.io.Serializable;

/**
 * Represents a single stock available on the simulated market.
 * Holds the stock symbol, company name, and current market price.
 */
public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;      // e.g. "AAPL"
    private String name;        // e.g. "Apple Inc."
    private double price;       // current fake market price
    private double previousPrice; // price before the last fluctuation (for GUI up/down coloring)

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.previousPrice = price;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    /** Percentage change since the last fluctuation. Positive = up, negative = down. */
    public double getChangePercent() {
        if (previousPrice == 0) return 0;
        return ((price - previousPrice) / previousPrice) * 100.0;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Simulates a small random fluctuation in price to make the
     * market feel alive between sessions. Range: -3% to +3%.
     */
    public void fluctuatePrice() {
        this.previousPrice = this.price;
        double changePercent = (Math.random() * 6) - 3; // -3 to +3
        double newPrice = price + (price * changePercent / 100);
        this.price = Math.round(newPrice * 100.0) / 100.0;
        if (this.price < 1.0) {
            this.price = 1.0; // floor so price never goes to zero/negative
        }
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-22s | $%,10.2f", symbol, name, price);
    }
}