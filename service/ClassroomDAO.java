package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Classroom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAO {

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM classrooms";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.getInt(1) == 0;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertClassrooms(List<Classroom> classrooms) {
        String sql = "INSERT OR IGNORE INTO classrooms(room_code, capacity) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Classroom c : classrooms) {
                ps.setString(1, c.getName());
                ps.setInt(2, c.getCapacity());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Classroom> getAllClassrooms() {
        List<Classroom> classrooms = new ArrayList<>();
        String sql = "SELECT room_code, capacity FROM classrooms";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                classrooms.add(
                        new Classroom(
                                rs.getString("room_code"),
                                rs.getInt("capacity")
                        )
                );
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return classrooms;
    }
}
