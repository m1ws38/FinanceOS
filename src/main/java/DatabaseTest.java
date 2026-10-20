import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {

    public static void main(String[] args) {

        // Создаём таблицы в базе данных
        DatabaseManager.initializeDatabase();

        // Проверяем подключение к SQLite
        try (Connection connection = DatabaseManager.getConnection()) {

            System.out.println("Подключение к SQLite успешно!");

        } catch (SQLException e) {

            System.out.println("Ошибка подключения: " + e.getMessage());

        }
    }
}