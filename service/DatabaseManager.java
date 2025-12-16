package Desktop_Application_Project_.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:exam_scheduler.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Students table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT UNIQUE,
                    name TEXT
                )
            """);

            // Classrooms table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS classrooms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_code TEXT UNIQUE,
                    capacity INTEGER
                )
            """);

            // Courses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_code TEXT UNIQUE,
                    name TEXT
                )
            """);

            // Attendance table (student-course relationship)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT NOT NULL,
                    course_code TEXT NOT NULL,
                    UNIQUE(student_id, course_code)
                )
            """);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("DB init failed", e);
        }
    }
}
