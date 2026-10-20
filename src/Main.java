import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static void addTransaction(
            Scanner sc,
            TransactionService service,
            TransactionType type
    ) {
        try {
            System.out.print("Введите сумму операции: ");
            BigDecimal amount = sc.nextBigDecimal();
            sc.nextLine();

            String category;

            while (true) {
                System.out.print("Введите категорию операции: ");
                category = sc.nextLine().trim();

                if (!category.isEmpty()) {
                    break;
                }

                System.out.println("Ошибка: категория не может быть пустой!");
            }

            System.out.print("Введите описание (необязательно): ");
            String description = sc.nextLine().trim();

            Transaction savedTransaction = service.addTransaction(
                    amount,
                    category,
                    description,
                    type
            );

            if (savedTransaction != null) {
                System.out.println(
                        "Операция успешно добавлена! ID: "
                                + savedTransaction.getId()
                );
            } else {
                System.out.println("Не удалось сохранить операцию!");
            }

        } catch (InputMismatchException e) {
            System.out.println("Ошибка! Введите корректную сумму.");
            sc.nextLine();

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int id;
        Scanner sc = new Scanner(System.in);
        TransactionManager manager = new TransactionManager();
        DatabaseManager.initializeDatabase();

        TransactionRepository repository = new TransactionRepository();

        TransactionService service = new TransactionService(
                manager,
                repository
        );

        for (Transaction transaction : repository.findAll()) {
            manager.addTransaction(transaction);
        }

        System.out.println("Данные из SQLite загружены!");
        while (true){
            int choice;
            System.out.println("""

        ========== FinanceOS ==========
        1. Добавить доход
        2. Добавить расход
        3. Показать все операции
        4. Показать баланс
        5. Удалить операцию
        6. Найти операцию по ID
        7. Статистика расходов
        0. Выход
        ===============================
        Выберите действие:
        """);
            try {
                choice = sc.nextInt();
                System.out.println("Вы выбрали: " + choice);
            } catch (InputMismatchException e) {
                System.out.print("Ошибка! Введите целое число.");
                sc.nextLine();
                continue;
            }
            switch (choice){
                case 1:
                    System.out.print("===Добавить доход===");
                    addTransaction(sc, service, TransactionType.INCOME);
                    break;
                case 2:
                    System.out.print("===Добавить расход===");
                    addTransaction(sc, service, TransactionType.EXPENSE);
                    break;
                case 3:
                    System.out.println("Показать все операции");
                    for (Transaction item : service.getAllTransactions()) {
                        System.out.println(
                                "ID: " + item.getId()
                                        + " | " + item.getType()
                                        + " | " + item.getAmount() + " ₽"
                                        + " | " + item.getCategory()
                        );
                    }
                    break;
                case 4:
                    System.out.print("Показать баланс");
                    System.out.print(service.getBalance());
                    break;
                case 5:
                    System.out.print("Удалить операцию\n");
                    System.out.print("Введите ID");
                    id = sc.nextInt();
                    if (service.deleteTransaction(id)) {
                        System.out.println("Операция успешно удалена!");
                    } else {
                        System.out.println("Не удалось удалить операцию!");
                    }
                    break;
                case 6:
                    System.out.print("Найти операцию по ID\n");
                    System.out.print("Введите ID");
                    try {
                        id = sc.nextInt();
                        Transaction found = service.findTransactionById(id);
                        if (found != null) {
                            System.out.println("ID: " + found.getId());
                            System.out.println("Сумма: " + found.getAmount());
                            System.out.println("Категория: " + found.getCategory());
                        }
                        else{
                            System.out.println("Операция не найдена");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Ошибка! Введите целое число.");
                        sc.nextLine();
                    }
                    break;
                case 7:
                    System.out.println("Статистика расходов");
                    System.out.println(service.getExpensesByCategory());
                    break;
                case 0:
                    System.out.println("Выход");
                    break;
                default:
                    System.out.println("Неизвестная команда");
            }
            if(choice==0){
                break;
            }
        }
    }
}
