import java.math.BigDecimal;
public class Main {
    public static void main(String[] args) {
        Transaction transaction = new Transaction(1,new BigDecimal("350.00"),"еда","обед",TransactionType.EXPENSE);
        System.out.println("id:" +transaction.getId());
        System.out.println("Тип:"+ transaction.getType());
        System.out.println("Сумма:"+transaction.getAmount());
        System.out.println("Категория:"+transaction.getCategory());
        System.out.println("Описание:"+transaction.getDescription());
        System.out.println("Дата:"+transaction.getCreatedAt());

        }
    }