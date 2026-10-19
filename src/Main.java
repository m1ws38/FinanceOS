import java.math.BigDecimal;
public class Main {
    public static void main(String[] args) {
        TransactionManager manager = new TransactionManager();
        Transaction transaction = new Transaction(1,new BigDecimal("50000.00"),"Зарплата",null,TransactionType.INCOME);
        Transaction transaction1 = new Transaction(2,new BigDecimal("1500.00"),"Продукты",null,TransactionType.EXPENSE);
        Transaction transaction2 = new Transaction(1,new BigDecimal("300.00"),"еда","обед",TransactionType.EXPENSE);
        manager.addTransaction(transaction);
        manager.addTransaction(transaction1);
        manager.addTransaction(transaction2);
        for(Transaction item : manager.getTransactions()){
            System.out.println("id:"+item.getId()+"|"+item.getType()+"|"+item.getAmount()+"|"+item.getCategory());
        }
        System.out.println(manager.removeTransaction(2));
        System.out.println(manager.removeTransaction(99));
        System.out.println(manager.getBalance());
        System.out.println("Текущий баланс: " + manager.getBalance());
        }
    }