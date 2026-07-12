import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles saving and loading user data (balance, portfolio, cost basis,
 * transaction history) to and from a plain text file, using
 * FileReader/FileWriter, so progress survives between program runs.
 *
 * File format (data/user_<username>.txt):
 *   BALANCE,<amount>
 *   PORTFOLIO,<symbol>,<quantity>
 *   COSTBASIS,<symbol>,<totalCostInvested>
 *   HISTORY,<type>,<symbol>,<qty>,<price>,<timestamp>
 */
public class DataManager {

    private static final String DATA_DIR = "data";

    public DataManager() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private String filePathFor(String username) {
        return DATA_DIR + File.separator + "user_" + username.toLowerCase() + ".txt";
    }

    public boolean userDataExists(String username) {
        return new File(filePathFor(username)).exists();
    }

    public void saveUser(User user) {
        String path = filePathFor(user.getUsername());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("BALANCE," + user.getBalance());
            writer.newLine();

            for (Map.Entry<String, Integer> entry : user.getPortfolio().entrySet()) {
                writer.write("PORTFOLIO," + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            for (Map.Entry<String, Double> entry : user.getCostBasis().entrySet()) {
                writer.write("COSTBASIS," + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            for (Transaction tx : user.getHistory()) {
                writer.write("HISTORY," + tx.toCsvLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public User loadUser(String username, double defaultStartingBalance) {
        String path = filePathFor(username);
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        User user = new User(username, defaultStartingBalance);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", 2);
                String tag = parts[0];

                if (tag.equals("BALANCE")) {
                    user.setBalance(Double.parseDouble(parts[1]));
                } else if (tag.equals("PORTFOLIO")) {
                    String[] pf = parts[1].split(",");
                    user.getPortfolio().put(pf[0], Integer.parseInt(pf[1]));
                } else if (tag.equals("COSTBASIS")) {
                    String[] cb = parts[1].split(",");
                    user.getCostBasis().put(cb[0], Double.parseDouble(cb[1]));
                } else if (tag.equals("HISTORY")) {
                    Transaction tx = Transaction.fromCsvLine(parts[1]);
                    user.getHistory().add(tx);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
            return null;
        }

        return user;
    }

    /**
     * Exports the transaction history as a clean, formatted plain-text
     * report (the "Export History" feature). Kept as .txt rather than a
     * true binary PDF to avoid pulling in an external PDF library —
     * the file is still a complete, readable, printable statement.
     */
    public File exportHistoryReport(User user, List<Transaction> transactions) {
        String path = DATA_DIR + File.separator + user.getUsername().toLowerCase() + "_history_report.txt";
        File outFile = new File(path);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("=================================================\n");
            writer.write("       STOCK TRADING PLATFORM - TRANSACTION REPORT\n");
            writer.write("=================================================\n");
            writer.write("Trader: " + user.getUsername() + "\n");
            writer.write("Current Cash Balance: $" + String.format("%,.2f", user.getBalance()) + "\n");
            writer.write("-------------------------------------------------\n");
            writer.write(String.format("%-12s %-6s %-8s %-10s %-12s%n",
                    "DATE", "TYPE", "SYMBOL", "QTY", "PRICE"));
            writer.write("-------------------------------------------------\n");

            for (Transaction tx : transactions) {
                writer.write(String.format("%-12s %-6s %-8s %-10d $%-11.2f%n",
                        tx.getFormattedDate(), tx.getType(), tx.getSymbol(),
                        tx.getQuantity(), tx.getPriceAtTransaction()));
            }

            writer.write("=================================================\n");
            writer.write("Report generated by the Stock Trading Platform.\n");
        } catch (IOException e) {
            System.out.println("Error exporting history: " + e.getMessage());
            return null;
        }

        return outFile;
    }
}