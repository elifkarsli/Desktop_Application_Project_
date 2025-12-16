package Desktop_Application_Project_.service;
import Desktop_Application_Project_.model.DomainModels.Student;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.model.DomainModels.ExamPeriod;

import Desktop_Application_Project_.model.DomainModels.*;

import java.util.List;

public class ConflictChecker {

    // Student conflict check
    public boolean hasStudentConflict(
            Course course,
            int day,
            int slot,
            ExamPeriod examPeriod,
            List<Course> allCourses
    ) {
        String[][] matrix = examPeriod.getExamMatrix();

        if (matrix[day - 1][slot - 1] == null) {
            return false;
        }

        String existingCourseCode = matrix[day - 1][slot - 1];

        for (Student student : course.getEnrolledStudents()) {
            for (Course otherCourse : allCourses) {
                if (otherCourse.getCourseCode().equals(existingCourseCode) &&
                        otherCourse.getEnrolledStudents().contains(student)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Classroom capacity control
    public boolean isRoomCapacityOk(Classroom classroom, int studentCount) {
        return classroom.getCapacity() >= studentCount;
    }
}
