package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM students";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.getInt(1) == 0;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertStudents(List<Student> students) {
        String sql = "INSERT INTO students(student_id) VALUES (?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Student s : students) {
                ps.setString(1, s.getId());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT student_id FROM students";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(new Student(rs.getString("student_id")));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return students;
    }
    public void clearTable() {
        String sql = "DELETE FROM students";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
