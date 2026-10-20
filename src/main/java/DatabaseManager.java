import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:financeos.db");
    }

    public static void initializeDatabase() {

        String sql = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY,
                    amount TEXT NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT,
                    type TEXT NOT NULL,
                    created_at TEXT NOT NULL
                );
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);

            System.out.println("Таблица transactions готова!");

        } catch (SQLException e) {
            System.out.println("Ошибка создания таблицы: " + e.getMessage());
        }
    }
}