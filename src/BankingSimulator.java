
import java.util.Scanner;
import java.math.BigDecimal;

public class BankingSimulator {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        BankService bank = new BankService();

        int choice = 0;

        do {

            System.out.println("\n======================================");
            System.out.println("       BANKING TRANSACTION SYSTEM");
            System.out.println("======================================");

            System.out.println("1. Create Bank Account");
            System.out.println("2. Display All Accounts");
            System.out.println("3. Deposit Money");
            System.out.println("4. Withdraw Money");
            System.out.println("5. Transfer Money");
            System.out.println("6. Transaction History");
            System.out.println("7. Generate Account Statement");
            System.out.println("8. Simultaneous Transactions");
            System.out.println("9. Exit");

            System.out.print("\nEnter your choice: ");

            if (!sc.hasNextInt()) {
                System.out.println("Enter a valid number!");
                sc.nextLine();
                continue;
            }

            choice = sc.nextInt();

            try {

                switch (choice) {

                    case 1:

                        System.out.print("Enter account number: ");
                        int number = sc.nextInt();

                        sc.nextLine();

                        System.out.print("Enter holder name: ");
                        String name = sc.nextLine();

                        System.out.print("Enter initial balance: ");
                        BigDecimal balance = sc.nextBigDecimal();

                        bank.createAccount(number, name, balance);

                        break;

                    case 2:

                        bank.displayAccounts();

                        break;

                    case 3:

                        System.out.print("Enter account number: ");
                        number = sc.nextInt();

                        System.out.print("Enter deposit amount: ");
                        BigDecimal deposit = sc.nextBigDecimal();

                        bank.deposit(number, deposit);

                        break;

                    case 4:

                        System.out.print("Enter account number: ");
                        number = sc.nextInt();

                        System.out.print("Enter withdrawal amount: ");
                        BigDecimal withdrawal = sc.nextBigDecimal();

                        bank.withdraw(number, withdrawal);

                        break;

                    case 5:

                        System.out.print("Enter sender account number: ");
                        int sender = sc.nextInt();

                        System.out.print("Enter receiver account number: ");
                        int receiver = sc.nextInt();

                        System.out.print("Enter transfer amount: ");
                        BigDecimal amount = sc.nextBigDecimal();

                        bank.transfer(sender, receiver, amount);

                        break;

                    case 6:

                        System.out.print("Enter account number: ");
                        number = sc.nextInt();

                        bank.transactionHistory(number);

                        break;

                    case 7:

                        System.out.print("Enter account number: ");
                        number = sc.nextInt();

                        bank.accountStatement(number);

                        break;

                    case 8:

                        System.out.println("\nSIMULTANEOUS TRANSACTION TEST");

                        System.out.println("\nBalances BEFORE transactions:");

                        bank.displayAccounts();

                        // Two threads withdraw from the SAME account.
                        // Only one withdrawal should succeed if
                        // the account has Rs.10000.

                        Thread t1 = new Thread(
                            new TransactionTask(
                                bank,
                                1001,
                                new BigDecimal("7000.00"),
                                "withdraw"
                            ),
                            "Thread-1"
                        );

                        Thread t2 = new Thread(
                            new TransactionTask(
                                bank,
                                1001,
                                new BigDecimal("7000.00"),
                                "withdraw"
                            ),
                            "Thread-2"
                        );

                        // Other simultaneous transactions

                        Thread t3 = new Thread(
                            new TransactionTask(
                                bank,
                                1003,
                                new BigDecimal("3000.00"),
                                "deposit"
                            ),
                            "Thread-3"
                        );

                        Thread t4 = new Thread(
                            new TransactionTask(
                                bank,
                                1004,
                                new BigDecimal("500.00"),
                                "withdraw"
                            ),
                            "Thread-4"
                        );

                        t1.start();
                        t2.start();
                        t3.start();
                        t4.start();

                        try {

                            t1.join();
                            t2.join();
                            t3.join();
                            t4.join();

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            System.out.println("Thread interrupted!");

                        }

                        System.out.println(
                            "\nBalances AFTER transactions:"
                        );

                        bank.displayAccounts();

                        System.out.println(
                            "\nAll transactions completed!"
                        );

                        break;

                    case 9:

                        System.out.println(
                            "\nThank you for using our Banking System!"
                        );

                        break;

                    default:

                        System.out.println("Invalid choice!");
                }

            } catch (java.util.InputMismatchException e) {

                System.out.println("Invalid input! Please enter correct data.");
                sc.nextLine();

            }

        } while (choice != 9);

        sc.close();
    }
}
