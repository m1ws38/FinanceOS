import java.math.BigDecimal;
import java.math.RoundingMode;

public class SavingsCalculator {
    public BigDecimal calculateMonthlyInterest(
            BigDecimal balance,
            BigDecimal annualRate
    ){
        return balance.multiply(annualRate).divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
    }
    public BigDecimal calculateFinalAmount(
            BigDecimal initialBalance,
            BigDecimal annualRate,
            int months,
            BigDecimal monthlyTopUp,
            boolean capitalization
    ) {
        BigDecimal balance = initialBalance;
        BigDecimal separateInterest = BigDecimal.ZERO;

        for (int i = 0; i < months; i++) {
            BigDecimal interest =
                    calculateMonthlyInterest(balance, annualRate);

            if (capitalization) {
                balance = balance.add(interest);
            } else {
                separateInterest = separateInterest.add(interest);
            }

            balance = balance.add(monthlyTopUp);
        }

        return balance.add(separateInterest);
    }
}
