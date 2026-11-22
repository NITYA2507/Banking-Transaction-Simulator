
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private long transactionId;
    private int accountNumber;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;

    public Transaction(long transactionId, int accountNumber,
                       String transactionType, BigDecimal amount,
                       String description, LocalDateTime transactionDate) {

        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {

        return "Transaction ID: " + transactionId +
               " | Account: " + accountNumber +
               " | Type: " + transactionType +
               " | Amount: Rs. " + amount +
               " | Description: " + description +
               " | Date: " + transactionDate;
    }
}
