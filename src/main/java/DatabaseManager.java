import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;

public class DatabaseManager {
    public static Path getDatabasePath() {
        String localAppData = System.getenv("LOCALAPPDATA");

        return Path.of(localAppData, "FinanceOS", "financeos.db");
    }

    public static Connection getConnection() throws SQLException {
        Path databasePath = getDatabasePath();

        try {
            Files.createDirectories(databasePath.getParent());
        } catch (IOException e) {
            throw new SQLException("Не удалось создать папку для базы данных", e);
        }

        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
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