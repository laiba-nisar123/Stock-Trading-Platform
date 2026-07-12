import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the trader (user) of the platform.
 * Tracks fake cash balance, current stock holdings (portfolio),
 * the total cost basis of each holding (so real Profit/Loss % can be
 * calculated against the price actually paid), and full trade history.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private double balance;
    private Map<String, Integer> portfolio;     // symbol -> quantity held
    private Map<String, Double> costBasis;       // symbol -> total $ invested in current holding
    private List<Transaction> history;

    public User(String username, double startingBalance) {
        this.username = username;
        this.balance = startingBalance;
        this.portfolio = new LinkedHashMap<>();
        this.costBasis = new LinkedHashMap<>();
        this.history = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public double getBalance() { return balance; }
    public Map<String, Integer> getPortfolio() { return portfolio; }
    public Map<String, Double> getCostBasis() { return costBasis; }
    public List<Transaction> getHistory() { return history; }

    public int getQuantityOwned(String symbol) {
        return portfolio.getOrDefault(symbol, 0);
    }

    /** Average price paid per share for a symbol currently held. Returns 0 if not held. */
    public double getAverageCost(String symbol) {
        int qty = getQuantityOwned(symbol);
        if (qty == 0) return 0;
        return costBasis.getOrDefault(symbol, 0.0) / qty;
    }

    /** Profit/Loss percentage for a held symbol, comparing current price to average cost. */
    public double getProfitLossPercent(String symbol, double currentPrice) {
        double avgCost = getAverageCost(symbol);
        if (avgCost == 0) return 0;
        return ((currentPrice - avgCost) / avgCost) * 100.0;
    }

    public void buyStock(Stock stock, int quantity) {
        double cost = stock.getPrice() * quantity;
        balance -= cost;
        portfolio.merge(stock.getSymbol(), quantity, Integer::sum);
        costBasis.merge(stock.getSymbol(), cost, Double::sum);
        history.add(new Transaction(Transaction.Type.BUY, stock.getSymbol(), quantity, stock.getPrice()));
    }

    public void sellStock(Stock stock, int quantity) {
        double proceeds = stock.getPrice() * quantity;
        balance += proceeds;

        String symbol = stock.getSymbol();
        int ownedBefore = portfolio.getOrDefault(symbol, 0);
        double avgCost = ownedBefore > 0 ? costBasis.getOrDefault(symbol, 0.0) / ownedBefore : 0;

        int remaining = ownedBefore - quantity;
        if (remaining <= 0) {
            portfolio.remove(symbol);
            costBasis.remove(symbol);
        } else {
            portfolio.put(symbol, remaining);
            costBasis.put(symbol, avgCost * remaining);
        }

        history.add(new Transaction(Transaction.Type.SELL, symbol, quantity, stock.getPrice()));
    }

    public void setBalance(double balance) { this.balance = balance; }
}