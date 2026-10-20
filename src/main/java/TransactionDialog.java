import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.math.BigDecimal;
import java.util.Optional;

public class TransactionDialog {

    private final TextField amountField = new TextField();
    private final TextField categoryField = new TextField();
    private final TextField descriptionField = new TextField();
    private final GridPane grid = new GridPane();
    private final TransactionType type;

    public TransactionDialog(TransactionType type) {
        this.type = type;
        amountField.setPromptText("Например: 5000");
        categoryField.setPromptText("Например: Зарплата");
        descriptionField.setPromptText("Необязательно");

        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Сумма:"), 0, 0);
        grid.add(amountField, 1, 0);

        grid.add(new Label("Категория:"), 0, 1);
        grid.add(categoryField, 1, 1);

        grid.add(new Label("Описание:"), 0, 2);
        grid.add(descriptionField, 1, 2);


    }
    public Optional<TransactionFormData> show() {
        Dialog<ButtonType> dialog = new Dialog<>();
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444;");

        dialog.setTitle("FinanceOS");
        dialog.setHeaderText(
                type == TransactionType.INCOME
                        ? "Добавление дохода"
                        : "Добавление расхода"
        );

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        Button okButton = (Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                BigDecimal amount = new BigDecimal(
                        amountField.getText().trim()
                );

                if (amount.signum() <= 0) {
                    errorLabel.setText("Сумма должна быть больше нуля!");
                    event.consume();
                    return;
                }

                if (categoryField.getText().isBlank()) {
                    errorLabel.setText("Укажите категорию!");
                    event.consume();
                    return;
                }

                errorLabel.setText("");

            } catch (NumberFormatException e) {
                errorLabel.setText("Введите корректную сумму!");
                event.consume();
            }
        });

        grid.add(errorLabel, 1, 3);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());

            TransactionFormData data = new TransactionFormData(
                    amount,
                    categoryField.getText().trim(),
                    descriptionField.getText().trim()
            );

            return Optional.of(data);
        }

        return Optional.empty();
    }
}


