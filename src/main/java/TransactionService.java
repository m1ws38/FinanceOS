import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class TransactionService {

    private final TransactionManager manager;
    private final TransactionRepository repository;

    public TransactionService(
            TransactionManager manager,
            TransactionRepository repository
    ) {
        this.manager = manager;
        this.repository = repository;
    }
    public BigDecimal getBalance() {
        return manager.getBalance();
    }
    public Transaction findTransactionById(int id) {
        return manager.findTransactionById(id);
    }
    public boolean deleteTransaction(int id) {
        return repository.deleteById(id) && manager.removeTransaction(id);
    }


    public Transaction addTransaction(
            BigDecimal amount,
            String category,
            String description,
            TransactionType type
    ) {
        Transaction transaction = new Transaction(
                0, amount, category, description, type
        );

        int generatedId = repository.save(transaction);

        if (generatedId <= 0) {
            return null;
        }

        Transaction savedTransaction = new Transaction(
                generatedId,
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getType(),
                transaction.getCreatedAt()
        );

        manager.addTransaction(savedTransaction);

        return savedTransaction;
    }
    public Transaction updateTransaction(
            int id,
            BigDecimal amount,
            String category,
            String description
    ) {
        // Находим старую операцию в памяти.
        Transaction existing = manager.findTransactionById(id);

        // Если такой операции нет, обновлять нечего.
        if (existing == null) {
            return null;
        }

        // Создаём исправленную версию операции.
        Transaction updated = new Transaction(
                existing.getId(),
                amount,
                category,
                description,
                existing.getType(),
                existing.getCreatedAt()
        );

        // Сначала сохраняем изменения в базе.
        boolean saved = repository.update(updated);

        if (!saved) {
            return null;
        }

        // После успешного сохранения обновляем память.
        manager.updateTransaction(updated);

        // Возвращаем исправленную операцию.
        return updated;
    }

    public ArrayList<Transaction> getAllTransactions() {
        return manager.getTransactions();
    }
    public HashMap<String, BigDecimal> getExpensesByCategory() {
        return manager.getExpensesByCategory();
    }
}