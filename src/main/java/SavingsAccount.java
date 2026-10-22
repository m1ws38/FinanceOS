import java.math.BigDecimal;

public class SavingsAccount {
    private int id;
    private String name;
    private BigDecimal balance;
    private BigDecimal annualRate;
    public SavingsAccount(int id, String name, BigDecimal balance, BigDecimal annualRate) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.annualRate = annualRate;
    }
    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public BigDecimal getBalance(){
        return balance;
    }
    public BigDecimal getAnnualRate(){
        return annualRate;
    }
}


