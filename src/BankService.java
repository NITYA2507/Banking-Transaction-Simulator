
import java.math.BigDecimal;

public class BankService {

    private BankDAO dao = new BankDAO();

    public void createAccount(int number, String name,
                              BigDecimal balance) {

        if (name == null || name.trim().isEmpty()) {
            System.out.println("Name cannot be empty!");
            return;
        }

        dao.createAccount(number, name, balance);
    }

    public void displayAccounts() {
        dao.displayAllAccounts();
    }

    public void deposit(int number, BigDecimal amount) {
        dao.deposit(number, amount);
    }

    public void withdraw(int number, BigDecimal amount) {
        dao.withdraw(number, amount);
    }

    public void transfer(int from, int to, BigDecimal amount) {
        dao.transfer(from, to, amount);
    }

    public void transactionHistory(int number) {
        dao.showHistory(number);
    }

    public void accountStatement(int number) {
        dao.generateStatement(number);
    }
}
