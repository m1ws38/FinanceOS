
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.math.BigDecimal;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

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

        balanceLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-text-fill: #4ade80;"
        );

        TableView<Transaction> transactionTable = new TableView<>();

        ObservableList<Transaction> tableTransactions =
                FXCollections.observableArrayList(
                        service.getAllTransactions()
                );

        FilteredList<Transaction> filteredTransactions =
                new FilteredList<>(tableTransactions, transaction -> true);

        SortedList<Transaction> sortedTransactions =
                new SortedList<>(filteredTransactions);

        sortedTransactions.comparatorProperty()
                .bind(transactionTable.comparatorProperty());

        transactionTable.setItems(sortedTransactions);

        Button editButton = new Button("Редактировать");

        editButton.setOnAction(event -> {
            Transaction selected =
                    transactionTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                return;
            }

            TransactionDialog dialog = new TransactionDialog(selected);

            dialog.show().ifPresent(data -> {
                Transaction updated = service.updateTransaction(
                        selected.getId(),
                        data.amount(),
                        data.category(),
                        data.description()
                );

                if (updated != null) {
                    for (int i = 0; i < tableTransactions.size(); i++) {
                        if (tableTransactions.get(i).getId() == updated.getId()) {
                            tableTransactions.set(i, updated);
                            break;
                        }
                    }

                    balanceLabel.setText(
                            "Текущий баланс: " + service.getBalance() + " ₽"
                    );
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.initOwner(stage);
                    error.setTitle("Ошибка");
                    error.setHeaderText("Не удалось изменить операцию");
                    error.setContentText("Попробуйте ещё раз.");
                    error.showAndWait();
                }
            });
        });

        Button deleteButton = new Button("Удалить операцию");

        deleteButton.setOnAction(event -> {
            Transaction selected =
                    transactionTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                return;
            }
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.initOwner(stage);
            confirmation.setTitle("Удаление операции");
            confirmation.setHeaderText("Удалить выбранную операцию?");
            confirmation.setContentText(
                    selected.getCategory()
                            + " — "
                            + selected.getAmount().toPlainString()
                            + " ₽"
            );

            ButtonType answer = confirmation.showAndWait()
                    .orElse(ButtonType.CANCEL);

            if (answer != ButtonType.OK) {
                return;
            }

            boolean deleted = service.deleteTransaction(selected.getId());

            if (deleted) {
                tableTransactions.remove(selected);

                balanceLabel.setText(
                        "Текущий баланс: " + service.getBalance() + " ₽"
                );
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.initOwner(stage);
                error.setTitle("Ошибка");
                error.setHeaderText("Не удалось удалить операцию");
                error.setContentText("Попробуйте ещё раз.");
                error.showAndWait();
            }
        });

        Button addIncomeButton = new Button("Добавить доход");


        addIncomeButton.setOnAction(event ->
                showTransactionDialog(
                        TransactionType.INCOME,
                        service,
                        balanceLabel,
                        tableTransactions
                )
        );


        TableColumn<Transaction, String> categoryColumn =
                new TableColumn<>("Категория");

        categoryColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getCategory()
                )
        );

        TableColumn<Transaction, BigDecimal> amountColumn =
                new TableColumn<>("Сумма");

        amountColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(
                        cell.getValue().getAmount()
                )
        );

        amountColumn.setCellFactory(column ->
                new TableCell<Transaction, BigDecimal>() {
                    @Override
                    protected void updateItem(BigDecimal amount, boolean empty) {
                        super.updateItem(amount, empty);

                        if (empty || amount == null) {
                            setText(null);
                        } else {
                            setText(amount.toPlainString() + " ₽");
                        }
                    }
                }
        );

        TableColumn<Transaction, String> typeColumn =
                new TableColumn<>("Тип");

        typeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getType() == TransactionType.INCOME
                                ? "Доход"
                                : "Расход"
                )
        );

        TableColumn<Transaction, String> descriptionColumn =
                new TableColumn<>("Описание");
        descriptionColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDescription()
                ));

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        TableColumn<Transaction, LocalDateTime> timeColumn =
                new TableColumn<>("Дата и время");
        timeColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(
                        cell.getValue().getCreatedAt()
                ));

        timeColumn.setCellFactory(column ->
                        new TableCell<Transaction, LocalDateTime>() {
                            @Override
                            protected void updateItem(LocalDateTime date, boolean empty) {
                                super.updateItem(date, empty);

                                if (empty || date == null) {
                                    setText(null);
                                } else {
                                    setText(date.format(dateFormatter));
                                }
                            }
                        });

        transactionTable.getColumns().add(typeColumn);
        transactionTable.getColumns().add(categoryColumn);
        transactionTable.getColumns().add(amountColumn);
        transactionTable.getColumns().add(descriptionColumn);
        transactionTable.getColumns().add(timeColumn);


        ComboBox<String> typeFilter = new ComboBox<>();

        typeFilter.getItems().addAll(
                "Все операции",
                "Доходы",
                "Расходы"
        );

        typeFilter.setValue("Все операции");

        VBox root = new VBox(15);
        Button addExpenseButton = new Button("Добавить расход");

        addExpenseButton.setOnAction(event ->
                showTransactionDialog(
                        TransactionType.EXPENSE,
                        service,
                        balanceLabel,
                        tableTransactions
                )
        );

        typeFilter.setOnAction(event -> {
            String selectedFilter = typeFilter.getValue();

            filteredTransactions.setPredicate(transaction -> {
                if ("Доходы".equals(selectedFilter)) {
                    return transaction.getType() == TransactionType.INCOME;
                }

                if ("Расходы".equals(selectedFilter)) {
                    return transaction.getType() == TransactionType.EXPENSE;
                }

                return true;
            });
        });
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #0f172a;");

        root.getChildren().addAll(
                title,
                subtitle,
                balanceLabel,
                addIncomeButton,
                addExpenseButton,
                typeFilter,
                transactionTable,
                editButton,
                deleteButton
        );

        Scene scene = new Scene(root, 800, 500);

        stage.setTitle("FinanceOS");
        stage.setScene(scene);
        stage.show();
    }

    private void showTransactionDialog(
            TransactionType type,
            TransactionService service,
            Label balanceLabel,
            ObservableList<Transaction> tableTransactions
    ) {
        TransactionDialog dialog = new TransactionDialog(type);

        dialog.show().ifPresent(data -> {
            Transaction transaction = service.addTransaction(
                    data.amount(),
                    data.category(),
                    data.description(),
                    type
            );

            if (transaction != null) {
                tableTransactions.add(transaction);
                balanceLabel.setText(
                        "Текущий баланс: " + service.getBalance() + " ₽"
                );
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.initOwner(balanceLabel.getScene().getWindow());
                error.setTitle("Ошибка сохранения");
                error.setHeaderText("Не удалось сохранить операцию");
                error.setContentText(
                        "Операция не добавлена. Попробуйте ещё раз."
                );
                error.showAndWait();
            }

        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
