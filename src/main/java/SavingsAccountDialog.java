import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.Optional;

public class SavingsAccountDialog {

    public Optional<SavingsAccountFormData> show() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("FinanceOS");
        dialog.setHeaderText("Новый накопительный счёт");

        FinanceOSApp.applyDialogTheme(dialog.getDialogPane());
        dialog.getDialogPane().getStyleClass().add("transaction-dialog");

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Счёт на отпуск");

        TextField balanceField = new TextField();
        balanceField.setPromptText("Например: 50000");

        TextField rateField = new TextField();
        rateField.setPromptText("Например: 12,5");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("expense-label");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Сумма на счёте:"), 0, 1);
        grid.add(balanceField, 1, 1);
        grid.add(new Label("Ставка, % годовых:"), 0, 2);
        grid.add(rateField, 1, 2);
        grid.add(errorLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL
        );

        Button okButton = (Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("primary");

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                BigDecimal balance = new BigDecimal(
                        balanceField.getText().trim().replace(',', '.')
                );
                BigDecimal rate = new BigDecimal(
                        rateField.getText().trim().replace(',', '.')
                );

                if (nameField.getText().isBlank()) {
                    errorLabel.setText("Введите название счёта");
                    event.consume();
                } else if (balance.signum() < 0 || rate.signum() < 0) {
                    errorLabel.setText("Сумма и ставка не могут быть отрицательными");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("Введите корректную сумму и ставку");
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return Optional.empty();
        }

        return Optional.of(new SavingsAccountFormData(
                nameField.getText().trim(),
                new BigDecimal(balanceField.getText().trim().replace(',', '.')),
                new BigDecimal(rateField.getText().trim().replace(',', '.'))
        ));
    }
}