
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
import javafx.scene.image.Image;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.DialogPane;

public class FinanceOSApp extends Application {
    private static boolean lightTheme = false;

    @Override
    public void start(Stage stage) {

        TransactionManager manager = new TransactionManager();
        TransactionRepository repository = new TransactionRepository();

        try {
            DatabaseManager.initializeDatabase();

            for (Transaction transaction : repository.findAll()) {
                manager.addTransaction(transaction);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Ошибка запуска FinanceOS");
            error.setHeaderText("Не удалось открыть данные приложения");
            error.setContentText(
                    "Не удалось создать или загрузить базу данных.\n"
                            + "Проверьте доступ к папке:\n"
                            + DatabaseManager.getDatabasePath().getParent()
            );
            error.showAndWait();

            javafx.application.Platform.exit();
            return;
        }

        TransactionService service = new TransactionService(manager, repository);


        Label title = new Label("FinanceOS");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Твой финансовый менеджер");
        subtitle.getStyleClass().add("app-subtitle");


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

        Label periodIncomeLabel = new Label();
        Label periodExpensesLabel = new Label();

        periodIncomeLabel.setText(
                "Доходы за период: "
                        + calculateIncome(tableTransactions, null, null) + " ₽"
        );
        periodExpensesLabel.setText(
                "Расходы за период: "
                        + calculateExpenses(tableTransactions, null, null) + " ₽"
        );

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

            confirmation.setGraphic(null);

            applyDialogTheme(confirmation.getDialogPane());
            confirmation.getDialogPane().getStyleClass().add("confirmation-dialog");

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

        DatePicker fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("С даты");

        DatePicker toDatePicker = new DatePicker();
        toDatePicker.setPromptText("По дату");

        javafx.event.EventHandler<javafx.event.ActionEvent> dateChanged = event -> {
            applyFilters(
                    filteredTransactions,
                    typeFilter.getValue(),
                    fromDatePicker.getValue(),
                    toDatePicker.getValue()
            );
            updatePeriodTotals(
                    tableTransactions,
                    fromDatePicker.getValue(),
                    toDatePicker.getValue(),
                    periodIncomeLabel,
                    periodExpensesLabel
            );
        };

        fromDatePicker.setOnAction(dateChanged);
        toDatePicker.setOnAction(dateChanged);

        VBox root = new VBox(15);

        Button resetFiltersButton = new Button("Сбросить фильтры");
        resetFiltersButton.setOnAction(event -> {
            typeFilter.setValue("Все операции");
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
            applyFilters(filteredTransactions,typeFilter.getValue(),fromDatePicker.getValue(),toDatePicker.getValue());
            updatePeriodTotals(
                    tableTransactions,
                    fromDatePicker.getValue(),
                    toDatePicker.getValue(),
                    periodIncomeLabel,
                    periodExpensesLabel
            );
        });

        tableTransactions.addListener(
                (javafx.collections.ListChangeListener<Transaction>) change ->
                        updatePeriodTotals(
                                tableTransactions,
                                fromDatePicker.getValue(),
                                toDatePicker.getValue(),
                                periodIncomeLabel,
                                periodExpensesLabel
                        )
        );

        Button addExpenseButton = new Button("Добавить расход");
        addExpenseButton.getStyleClass().add("expense");

        addExpenseButton.setOnAction(event ->
                showTransactionDialog(
                        TransactionType.EXPENSE,
                        service,
                        balanceLabel,
                        tableTransactions
                )
        );

        typeFilter.setOnAction(event -> {
            applyFilters(
                    filteredTransactions,
                    typeFilter.getValue(),
                    fromDatePicker.getValue(),
                    toDatePicker.getValue()
            );
        });
        root.setAlignment(Pos.CENTER);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(20));

        Button openCalculatorButton = new Button("Мои накопления");

        VBox heading = new VBox(4, title, subtitle);

        Button themeButton = new Button("☾");
        themeButton.getStyleClass().add("theme-toggle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(
                20, heading, openCalculatorButton, headerSpacer, themeButton
        );
        header.setAlignment(Pos.CENTER_LEFT);

        balanceLabel.getStyleClass().add("balance-label");

        HBox addButtons = new HBox(10, addIncomeButton, addExpenseButton);
        addButtons.setAlignment(Pos.CENTER_LEFT);

        HBox filters = new HBox(
                10,
                typeFilter,
                fromDatePicker,
                toDatePicker,
                resetFiltersButton
        );
        filters.setAlignment(Pos.CENTER_LEFT);

        HBox periodTotals = new HBox(30, periodIncomeLabel, periodExpensesLabel);
        periodIncomeLabel.getStyleClass().add("income-label");
        periodExpensesLabel.getStyleClass().add("expense-label");

        HBox tableActions = new HBox(10, editButton, deleteButton);
        tableActions.setAlignment(Pos.CENTER_LEFT);

        transactionTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        VBox.setVgrow(transactionTable, Priority.ALWAYS);

        root.getChildren().addAll(
                header,
                balanceLabel,
                addButtons,
                filters,
                transactionTable,
                periodTotals,
                tableActions
        );

        Scene scene = new Scene(root, 1000, 650);

        VBox calculatorRoot = new VBox(15);
        calculatorRoot.setAlignment(Pos.TOP_CENTER);
        calculatorRoot.setPadding(new Insets(20));

        Label calculatorTitle = new Label("Вклады и накопительные счета");
        calculatorTitle.getStyleClass().add("app-title");

        Button backButton = new Button("← К операциям");
        calculatorRoot.getChildren().addAll(backButton, calculatorTitle);

        Scene calculatorScene = new Scene(calculatorRoot, 1000, 650);



        openCalculatorButton.setOnAction(
                event -> stage.setScene(calculatorScene)
        );
        backButton.setOnAction(
                event -> stage.setScene(scene)
        );

        stage.setTitle("FinanceOS");
        stage.setScene(scene);
        stage.getIcons().add(
                new Image(
                        FinanceOSApp.class.getResourceAsStream("/financeos.png")
                )
        );
        addIncomeButton.getStyleClass().add("primary");
        deleteButton.getStyleClass().add("danger");

        String stylesheet = FinanceOSApp.class
                .getResource("/financeos.css")
                .toExternalForm();

        scene.getStylesheets().add(stylesheet);
        calculatorScene.getStylesheets().add(stylesheet);

        String lightStylesheet = FinanceOSApp.class
                .getResource("/financeos-light.css")
                .toExternalForm();

        themeButton.setOnAction(event -> {
            if (scene.getStylesheets().contains(lightStylesheet)) {
                lightTheme = false;
                scene.getStylesheets().remove(lightStylesheet);
                calculatorScene.getStylesheets().remove(lightStylesheet);
                themeButton.setText("☾");
            } else {
                lightTheme = true;
                scene.getStylesheets().add(lightStylesheet);
                calculatorScene.getStylesheets().add(lightStylesheet);
                themeButton.setText("☀");
            }
        });

        stage.show();
    }

    public static void applyDialogTheme(DialogPane pane) {
        pane.getStylesheets().add(
                FinanceOSApp.class
                        .getResource("/financeos.css")
                        .toExternalForm()
        );

        if (lightTheme) {
            pane.getStylesheets().add(
                    FinanceOSApp.class
                            .getResource("/financeos-light.css")
                            .toExternalForm()
            );
        }
    }

    private boolean matchesDateRange(
            Transaction transaction,
            LocalDate from,
            LocalDate to
    ) {
        LocalDate date = transaction.getCreatedAt().toLocalDate();

        if(from!=null && date.isBefore(from)){
            return false;
        }
        if(to!=null && date.isAfter(to)){
            return false;
        }



        return true;
    }

    private BigDecimal calculateIncome(
            ObservableList<Transaction> transactions,
            LocalDate from,
            LocalDate to
    ) {
        BigDecimal total = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if(transaction.getType() == TransactionType.INCOME && matchesDateRange(transaction,from,to)){
                total = total.add(transaction.getAmount());
            }
        }

        return total;
    }

    private BigDecimal calculateExpenses(
            ObservableList<Transaction> transactions,
            LocalDate from,
            LocalDate to
    ) {
        BigDecimal total = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if(transaction.getType() == TransactionType.EXPENSE && matchesDateRange(transaction,from,to)){
                total = total.add(transaction.getAmount());
            }
        }

        return total;
    }

    private void updatePeriodTotals(
            ObservableList<Transaction> transactions,
            LocalDate from,
            LocalDate to,
            Label incomeLabel,
            Label expensesLabel
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            return;
        }
        incomeLabel.setText(
                "Доходы за период: "
                        + calculateIncome(transactions, from, to) + " ₽"
        );

        expensesLabel.setText(
                "Расходы за период: "
                        + calculateExpenses(transactions, from, to) + " ₽"
        );
    }

    private void applyFilters(
            FilteredList<Transaction> filteredTransactions,
            String selectedType,
            LocalDate from,
            LocalDate to
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Некорректный период");
            warning.setHeaderText("Проверьте выбранные даты");
            warning.setContentText(
                    "Начальная дата не должна быть позже конечной.\n"
                            + "Фильтр не применён: в таблице осталась предыдущая выборка."
            );
            warning.showAndWait();
            return;
        }

        filteredTransactions.setPredicate(transaction -> {
            if ("Доходы".equals(selectedType)
                    && transaction.getType() != TransactionType.INCOME) {
                return false;
            }

            if ("Расходы".equals(selectedType)
                    && transaction.getType() != TransactionType.EXPENSE) {
                return false;
            }


            return matchesDateRange(transaction, from, to);
        });
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
