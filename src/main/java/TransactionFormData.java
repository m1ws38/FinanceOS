import java.math.BigDecimal;

public record TransactionFormData(
        BigDecimal amount,
        String category,
        String description
) {
}
