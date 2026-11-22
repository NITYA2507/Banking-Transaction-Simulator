
import java.math.BigDecimal;

public class TransactionTask implements Runnable {

    private BankService bank;
    private int accountNumber;
    private BigDecimal amount;
    private String operation;

    public TransactionTask(BankService bank, int accountNumber,
                           BigDecimal amount, String operation) {

        this.bank = bank;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.operation = operation;
    }

    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();

        System.out.println("\n" + threadName + " started.");

        if (operation.equalsIgnoreCase("deposit")) {

            bank.deposit(accountNumber, amount);

        } else if (operation.equalsIgnoreCase("withdraw")) {

            bank.withdraw(accountNumber, amount);

        } else {

            System.out.println("Invalid transaction operation!");
        }

        System.out.println(threadName + " completed.");
    }
}
