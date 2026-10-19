import java.math.BigDecimal;
import java.util.ArrayList;
public class TransactionManager {
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    public boolean addTransaction(Transaction transaction) {

        for(Transaction item : transactions){
            if (transaction.getId() == item.getId()){
                return false;
            }
        }
        transactions.add(transaction);
        return true;
    }

    public ArrayList<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }
    public BigDecimal getBalance() {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction item : transactions){
            if(item.getType()==TransactionType.EXPENSE){
                balance = balance.subtract(item.getAmount());
            }
            else{
                balance = balance.add(item.getAmount());
            }
        }
        return balance;
    }
    public boolean removeTransaction(int id) {
        for(int i=0;i<transactions.size();i++){
            if(transactions.get(i).getId()==id){
                transactions.remove(i);
                return true;
            }
        }
        return false;
    }
}
