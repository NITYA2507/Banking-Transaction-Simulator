
import java.sql.*;
import java.math.BigDecimal;

public class BankDAO {

    // CREATE ACCOUNT
    public boolean createAccount(int number, String name,
                                 BigDecimal balance) {

        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Invalid initial balance!");
            return false;
        }

        String sql = "INSERT INTO accounts " +
                     "(account_number, holder_name, balance) " +
                     "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, number);
            ps.setString(2, name);
            ps.setBigDecimal(3, balance);

            ps.executeUpdate();

            System.out.println("Account created successfully!");
            return true;

        } catch (SQLException e) {
            System.out.println("Account creation failed: " + e.getMessage());
            return false;
        }
    }

    // FIND ACCOUNT
    public BankAccount findAccount(int number) {

        String sql =
            "SELECT * FROM accounts WHERE account_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, number);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new BankAccount(
                        rs.getInt("account_number"),
                        rs.getString("holder_name"),
                        rs.getBigDecimal("balance")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    // DISPLAY ALL ACCOUNTS
    public void displayAllAccounts() {

        String sql =
            "SELECT * FROM accounts ORDER BY account_number";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            boolean found = false;

            while (rs.next()) {

                found = true;

                BankAccount account = new BankAccount(
                    rs.getInt("account_number"),
                    rs.getString("holder_name"),
                    rs.getBigDecimal("balance")
                );

                account.displayAccount();
            }

            if (!found) {
                System.out.println("No accounts available.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // DEPOSIT MONEY
    public boolean deposit(int number, BigDecimal amount) {

        if (amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {

            System.out.println("Invalid deposit amount!");
            return false;
        }

        String lockSQL =
            "SELECT balance FROM accounts " +
            "WHERE account_number = ? FOR UPDATE";

        String updateSQL =
            "UPDATE accounts SET balance = balance + ? " +
            "WHERE account_number = ?";

        String historySQL =
            "INSERT INTO transactions " +
            "(account_number, transaction_type, amount, description) " +
            "VALUES (?, 'DEPOSIT', ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                BigDecimal oldBalance;

                // Lock account row
                try (PreparedStatement ps = con.prepareStatement(lockSQL)) {

                    ps.setInt(1, number);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException("Account not found!");
                        }

                        oldBalance = rs.getBigDecimal("balance");
                    }
                }

                // Update balance
                try (PreparedStatement ps = con.prepareStatement(updateSQL)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, number);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Deposit failed!");
                    }
                }

                // Save transaction history
                try (PreparedStatement ps =
                     con.prepareStatement(historySQL)) {

                    ps.setInt(1, number);
                    ps.setBigDecimal(2, amount);
                    ps.setString(3, "Money deposited");

                    ps.executeUpdate();
                }

                con.commit();

                System.out.println("Deposit successful!");
                System.out.println("Previous Balance: Rs. " + oldBalance);
                System.out.println("New Balance: Rs. " + oldBalance.add(amount));

                return true;

            } catch (Exception e) {

                con.rollback();
                System.out.println("Deposit failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {

            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // WITHDRAW MONEY
    public boolean withdraw(int number, BigDecimal amount) {

        if (amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {

            System.out.println("Invalid withdrawal amount!");
            return false;
        }

        String lockSQL =
            "SELECT balance FROM accounts " +
            "WHERE account_number = ? FOR UPDATE";

        String updateSQL =
            "UPDATE accounts SET balance = balance - ? " +
            "WHERE account_number = ? AND balance >= ?";

        String historySQL =
            "INSERT INTO transactions " +
            "(account_number, transaction_type, amount, description) " +
            "VALUES (?, 'WITHDRAWAL', ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                BigDecimal oldBalance;

                // Lock account
                try (PreparedStatement ps = con.prepareStatement(lockSQL)) {

                    ps.setInt(1, number);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException("Account not found!");
                        }

                        oldBalance = rs.getBigDecimal("balance");
                    }
                }

                // Check balance
                if (oldBalance.compareTo(amount) < 0) {
                    throw new SQLException("Insufficient balance!");
                }

                // Deduct amount
                try (PreparedStatement ps = con.prepareStatement(updateSQL)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, number);
                    ps.setBigDecimal(3, amount);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Withdrawal failed!");
                    }
                }

                // Save history
                try (PreparedStatement ps =
                     con.prepareStatement(historySQL)) {

                    ps.setInt(1, number);
                    ps.setBigDecimal(2, amount);
                    ps.setString(3, "Money withdrawn");

                    ps.executeUpdate();
                }

                con.commit();

                System.out.println("Withdrawal successful!");
                System.out.println("Previous Balance: Rs. " + oldBalance);
                System.out.println("New Balance: Rs. " + oldBalance.subtract(amount));

                return true;

            } catch (Exception e) {

                con.rollback();
                System.out.println("Withdrawal failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {

            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // TRANSFER MONEY
    public boolean transfer(int from, int to, BigDecimal amount) {

        if (from == to || amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {

            System.out.println("Invalid transfer details!");
            return false;
        }

        String lockSQL =
            "SELECT account_number, balance FROM accounts " +
            "WHERE account_number IN (?, ?) " +
            "ORDER BY account_number FOR UPDATE";

        String debitSQL =
            "UPDATE accounts SET balance = balance - ? " +
            "WHERE account_number = ? AND balance >= ?";

        String creditSQL =
            "UPDATE accounts SET balance = balance + ? " +
            "WHERE account_number = ?";

        String historySQL =
            "INSERT INTO transactions " +
            "(account_number, transaction_type, amount, description) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                BigDecimal senderBalance = null;
                int found = 0;

                // Lock both accounts in account-number order
                try (PreparedStatement ps = con.prepareStatement(lockSQL)) {

                    ps.setInt(1, from);
                    ps.setInt(2, to);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            found++;

                            if (rs.getInt("account_number") == from) {
                                senderBalance = rs.getBigDecimal("balance");
                            }
                        }
                    }
                }

                if (found != 2) {
                    throw new SQLException("Account not found!");
                }

                if (senderBalance.compareTo(amount) < 0) {
                    throw new SQLException("Insufficient balance!");
                }

                // Deduct from sender
                try (PreparedStatement ps = con.prepareStatement(debitSQL)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, from);
                    ps.setBigDecimal(3, amount);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Sender debit failed!");
                    }
                }

                // Add to receiver
                try (PreparedStatement ps = con.prepareStatement(creditSQL)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, to);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Receiver credit failed!");
                    }
                }

                // Sender history
                try (PreparedStatement ps =
                     con.prepareStatement(historySQL)) {

                    ps.setInt(1, from);
                    ps.setString(2, "TRANSFER_SENT");
                    ps.setBigDecimal(3, amount);
                    ps.setString(4, "Transferred to account " + to);

                    ps.executeUpdate();
                }

                // Receiver history
                try (PreparedStatement ps =
                     con.prepareStatement(historySQL)) {

                    ps.setInt(1, to);
                    ps.setString(2, "TRANSFER_RECEIVED");
                    ps.setBigDecimal(3, amount);
                    ps.setString(4, "Received from account " + from);

                    ps.executeUpdate();
                }

                con.commit();

                System.out.println("Transfer successful!");
                System.out.println("Previous sender balance: Rs. " + senderBalance);
                System.out.println("New sender balance: Rs. " + senderBalance.subtract(amount));
                System.out.println("Amount transferred: Rs. " + amount);

                return true;

            } catch (Exception e) {

                con.rollback();
                System.out.println("Transfer failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {

            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // TRANSACTION HISTORY
    public void showHistory(int number) {

        String sql =
            "SELECT * FROM transactions " +
            "WHERE account_number = ? " +
            "ORDER BY transaction_date, transaction_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, number);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n========== TRANSACTION HISTORY ==========");

                while (rs.next()) {

                    found = true;

                    Transaction t = new Transaction(
                        rs.getLong("transaction_id"),
                        rs.getInt("account_number"),
                        rs.getString("transaction_type"),
                        rs.getBigDecimal("amount"),
                        rs.getString("description"),
                        rs.getTimestamp("transaction_date").toLocalDateTime()
                    );

                    System.out.println(t);
                }

                if (!found) {
                    System.out.println("No transactions found.");
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ACCOUNT STATEMENT
    public void generateStatement(int number) {

        BankAccount account = findAccount(number);

        if (account == null) {
            System.out.println("Account not found!");
            return;
        }

        System.out.println("\n========== ACCOUNT STATEMENT ==========");

        account.displayAccount();

        showHistory(number);
    }
}
