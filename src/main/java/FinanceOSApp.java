
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;

public class FinanceOSApp extends Application {

    @Override
    public void start(Stage stage) {

        DatabaseManager.initializeDatabase();

        TransactionManager manager = new TransactionManager();
        TransactionRepository repository = new TransactionRepository();

        for (Transaction transaction : repository.findAll()) {
            manager.addTransaction(transaction);

        }
        System.out.println("===== ПРОВЕРКА ЗАГРУЗКИ =====");

        for (Transaction transaction : manager.getTransactions()) {
            System.out.println(
                    "ID: " + transaction.getId()
                            + " | Сумма: " + transaction.getAmount().toPlainString()
                            + " | Тип: " + transaction.getType()
            );
        }

        System.out.println(
                "Всего операций: " + manager.getTransactions().size()
        );
        TransactionService service = new TransactionService(manager, repository);
        System.out.println(
                "[START] Баланс: " + service.getBalance().toPlainString()
        );


        Label title = new Label("FinanceOS");
        title.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label subtitle = new Label("Твой финансовый менеджер");
        subtitle.setStyle("-fx-text-fill: #94a3b8;");

        Label balanceLabel = new Label(
                "Текущий баланс: " + service.getBalance() + " ₽"
        );
        System.out.println(
                "[UI] Баланс на экране: " + balanceLabel.getText()
        );

        balanceLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-text-fill: #4ade80;"
        );

        Button addIncomeButton = new Button("Добавить доход");


        addIncomeButton.setOnAction(event -> {
            TransactionDialog dialog = new TransactionDialog(TransactionType.INCOME);

            dialog.show().ifPresent(data -> {
                System.out.println(
                        "[BEFORE SAVE] Баланс: " + service.getBalance().toPlainString()
                );
                Transaction transaction = service.addTransaction(
                        data.amount(),
                        data.category(),
                        data.description(),
                        TransactionType.INCOME
                );
                System.out.println(
                        "[AFTER SAVE] Баланс: " + service.getBalance().toPlainString()
                );

                if (transaction != null) {
                    balanceLabel.setText(
                            "Текущий баланс: " + service.getBalance() + " ₽"
                    );

                    System.out.println(
                            "Доход успешно добавлен! ID: " + transaction.getId()
                    );
                } else {
                    System.out.println("Не удалось сохранить доход!");
                }
            });
        });


        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #0f172a;");

        root.getChildren().addAll(
                title,
                subtitle,
                balanceLabel,
                addIncomeButton
        );

        Scene scene = new Scene(root, 800, 500);

        stage.setTitle("FinanceOS");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
