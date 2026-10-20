import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionRepository {

    public int save(Transaction transaction) {

        String sql = """
            INSERT INTO transactions
            (amount, category, description, type, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, transaction.getAmount().toPlainString());
            statement.setString(2, transaction.getCategory());
            statement.setString(3, transaction.getDescription());
            statement.setString(4, transaction.getType().toString());
            statement.setString(5, transaction.getCreatedAt().toString());

            int rows = statement.executeUpdate();

            if (rows == 1) {
                try (PreparedStatement idStatement =
                             connection.prepareStatement("SELECT last_insert_rowid()");
                     ResultSet result = idStatement.executeQuery()) {

                    if (result.next()) {
                        return result.getInt(1);
                    }
                }
            }

            return -1;

        } catch (SQLException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
            return -1;
        }
    }
    public boolean deleteById(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Ошибка удаления: " + e.getMessage());
            return false;
        }
    }
    public ArrayList<Transaction> findAll() {

        ArrayList<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT * FROM transactions";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                int id = result.getInt("id");

                BigDecimal amount = new BigDecimal(
                        result.getString("amount")
                );

                String category = result.getString("category");
                String description = result.getString("description");

                TransactionType type = TransactionType.valueOf(
                        result.getString("type")
                );

                LocalDateTime createdAt = LocalDateTime.parse(
                        result.getString("created_at")
                );

                Transaction transaction = new Transaction(
                        id, amount, category, description, type, createdAt
                );

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка загрузки транзакций из базы данных", e
            );
        }

        return transactions;
    }
}