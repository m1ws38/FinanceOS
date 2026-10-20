import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private final int id;
    private final BigDecimal amount;
    private final String category;
    private final String description;
    private final LocalDateTime createdAt;
    private final TransactionType type;

    // Конструктор для новой транзакции
    public Transaction(int id, BigDecimal amount, String category,
                       String description, TransactionType type) {

        this(id, amount, category, description, type, LocalDateTime.now());
    }

    // Конструктор для восстановления транзакции из БД
    public Transaction(int id, BigDecimal amount, String category,
                       String description, TransactionType type,
                       LocalDateTime createdAt) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Сумма операции должна быть больше нуля!"
            );
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Категория операции не может быть пустой!"
            );
        }

        this.id = id;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
        this.type = type;

    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TransactionType getType() {
        return type;
    }
}