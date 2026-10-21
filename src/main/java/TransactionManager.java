import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

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
        Transaction found = findTransactionById(id);
        if(found != null){
            return transactions.remove(found);
        }
        return false;
    }
    public Transaction findTransactionById(int id) {
        for(int i=0;i<transactions.size();i++){
            if(transactions.get(i).getId() == id){
                return transactions.get(i);
            }
        }
        return null;
    }
    public boolean updateTransaction(Transaction updatedTransaction) {
        for(int i=0;i<transactions.size();i++){
            if (transactions.get(i).getId() == updatedTransaction.getId()){
                transactions.set(i,updatedTransaction);
                return true;
            }
        }
        return false;
    }
    public HashMap<String, BigDecimal> getExpensesByCategory() {
        HashMap<String, BigDecimal> expenses = new HashMap<>();
        for(Transaction item : transactions){
            if(item.getType()==TransactionType.EXPENSE){
                BigDecimal oldAmount = expenses.getOrDefault(item.getCategory(), BigDecimal.ZERO);
                BigDecimal newAmount = oldAmount.add(item.getAmount());
                expenses.put(item.getCategory(),newAmount);
            }
        }
        return expenses;
    }
}
