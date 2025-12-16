package Desktop_Application_Project_.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class AttendanceDAO {

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM attendance";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // CSV -> DB
    public void insertAttendance(List<String[]> attendance) {
        String sql = """
            INSERT OR IGNORE INTO attendance(student_id, course_code)
            VALUES (?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (String[] row : attendance) {
                ps.setString(1, row[0]);
                ps.setString(2, row[1]);
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // DB -> Memory
    public Map<String, List<String>> getAllEnrollments() {
        Map<String, List<String>> map = new HashMap<>();

        String sql = "SELECT student_id, course_code FROM attendance";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map
                        .computeIfAbsent(rs.getString("course_code"), k -> new ArrayList<>())
                        .add(rs.getString("student_id"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return map;
    }
}
