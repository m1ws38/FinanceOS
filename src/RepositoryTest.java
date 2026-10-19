import java.math.BigDecimal;

public class RepositoryTest {

    public static void main(String[] args) {

        DatabaseManager.initializeDatabase();

        TransactionRepository repository = new TransactionRepository();

        Transaction transaction = new Transaction(
                1,
                new BigDecimal("5000"),
                "Зарплата",
                "Первая зарплата",
                TransactionType.INCOME
        );

        int result = repository.save(transaction);

        System.out.println("Транзакция сохранена: " + result);
        System.out.println("Транзакций в базе: " + repository.findAll().size());
        boolean deleted = repository.deleteById(1);

        System.out.println("Транзакция удалена: " + deleted);
        System.out.println("Транзакций в базе: " + repository.findAll().size());
    }
}