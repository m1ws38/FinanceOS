import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final int id;
    private final BigDecimal amount;
    private final String category;
    private final String description;
    private final LocalDateTime createdAt;
    private final TransactionType type;
    public Transaction(int id, BigDecimal amount, String category, String description, TransactionType type) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Сумма операции должна быть больше нуля!"
            );
        }
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.type = type;
    }
    public int getId() {
        return id;
    }
    public String getCategory(){
        return category;
    }
    public String getDescription(){
        return description;
    }
    public BigDecimal getAmount(){
        return amount;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public TransactionType getType(){
        return type;
    }
}
