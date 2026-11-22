
import java.math.BigDecimal;

public class BankAccount {

    private int accountNumber;
    private String holderName;
    private BigDecimal balance;

    public BankAccount(int accountNumber, String holderName,
                       BigDecimal balance) {

        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void displayAccount() {

        System.out.println("-----------------------------------");
        System.out.println("Account Number : " + accountNumber);
        System.out.println("Holder Name    : " + holderName);
        System.out.println("Balance        : Rs. " + balance);
        System.out.println("-----------------------------------");
    }
}
