package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseEnrollmentService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public void loadEnrollments(
            List<Course> courses,
            List<Student> students
    ) {
        Map<String, List<String>> enrollments =
                attendanceDAO.getAllEnrollments();

        Map<String, Student> studentMap = new HashMap<>();
        for (Student s : students) {
            studentMap.put(s.getId(), s);
        }

        for (Course course : courses) {
            List<String> studentIds =
                    enrollments.get(course.getCourseCode());

            if (studentIds == null) continue;

            for (String studentId : studentIds) {
                Student s = studentMap.get(studentId);
                if (s != null) {
                    course.enrollStudent(s);
                }
            }
        }
    }

}
