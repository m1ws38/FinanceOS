import java.math.BigDecimal;

public record SavingsAccountFormData(
        String name,
        BigDecimal balance,
        BigDecimal annualRate
) {

}