import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SavingsAccountRepository {

    public SavingsAccount save(SavingsAccountFormData data) {
        String sql = """
                INSERT INTO savings_accounts (name, balance, annual_rate)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, data.name());
            statement.setString(2, data.balance().toPlainString());
            statement.setString(3, data.annualRate().toPlainString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                            "База не вернула ID нового счёта"
                    );
                }

                return new SavingsAccount(
                        result.getInt("id"),
                        data.name(),
                        data.balance(),
                        data.annualRate()
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось сохранить накопительный счёт", e
            );
        }
    }
    public List<SavingsAccount> findAll() {
        String sql = """
            SELECT id, name, balance, annual_rate
            FROM savings_accounts
            ORDER BY id
            """;

        List<SavingsAccount> accounts = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                accounts.add(new SavingsAccount(
                        result.getInt("id"),
                        result.getString("name"),
                        new BigDecimal(result.getString("balance")),
                        new BigDecimal(result.getString("annual_rate"))
                ));
            }

            return accounts;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось загрузить накопительные счета", e
            );
        }
    }
}