import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages the collection of stocks available on the simulated market.
 */
public class StockMarket {

    private List<Stock> stocks;

    public StockMarket() {
        stocks = new ArrayList<>();
        loadSampleStocks();
    }

    /**
     * Pre-populates the market with well-known sample companies
     * and fake starting prices.
     */
    private void loadSampleStocks() {
        stocks.add(new Stock("AAPL", "Apple Inc.", 190.25));
        stocks.add(new Stock("GOOGL", "Alphabet Inc.", 142.60));
        stocks.add(new Stock("MSFT", "Microsoft Corp.", 415.75));
        stocks.add(new Stock("AMZN", "Amazon.com Inc.", 178.90));
        stocks.add(new Stock("TSLA", "Tesla Inc.", 245.30));
        stocks.add(new Stock("NFLX", "Netflix Inc.", 610.15));
        stocks.add(new Stock("META", "Meta Platforms", 485.40));
    }

    public List<Stock> getAllStocks() {
        return stocks;
    }

    public Optional<Stock> findStock(String symbol) {
        return stocks.stream()
                .filter(s -> s.getSymbol().equalsIgnoreCase(symbol))
                .findFirst();
    }

    /**
     * Applies a small random fluctuation to every stock's price.
     * Called once per session to simulate a "live" market.
     */
    public void simulateMarketMovement() {
        for (Stock s : stocks) {
            s.fluctuatePrice();
        }
    }

    public void displayMarket() {
        System.out.println("\n===================== MARKET WATCH =====================");
        System.out.printf("%-6s | %-22s | %s%n", "SYM", "COMPANY", "PRICE");
        System.out.println("----------------------------------------------------------");
        for (Stock s : stocks) {
            System.out.println(s);
        }
        System.out.println("==========================================================");
    }
}