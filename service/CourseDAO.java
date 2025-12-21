package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM courses";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.getInt(1) == 0;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertCourses(List<Course> courses) {
        String sql = "INSERT INTO courses(course_code) VALUES (?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Course c : courses) {
                ps.setString(1, c.getCourseCode());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();

        String sql = "SELECT course_code FROM courses";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                courses.add(new Course(rs.getString("course_code")));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return courses;
    }
    public void clearTable() {
        String sql = "DELETE FROM courses";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
