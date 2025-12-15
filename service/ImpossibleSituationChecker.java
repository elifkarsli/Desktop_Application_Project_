package Desktop_Application_Project_.service;

import Desktop_Application_Project_.ExamPeriod;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Classroom;

import java.util.List;

public class ImpossibleSituationChecker {

    public void check(
            ExamPeriod examPeriod,
            List<Course> enrolledCourses,
            List<Classroom> classrooms,
            int fixedExamCount
    ) {

        checkSlotCount(examPeriod, enrolledCourses.size(), fixedExamCount);
        checkClassroomCapacity(enrolledCourses, classrooms);
    }

    // Checks whether the total number of available slots is sufficient
    // for all fixed and regular exams (impossible otherwise)
    private void checkSlotCount(
            ExamPeriod examPeriod,
            int regularExamCount,
            int fixedExamCount
    ) {
        int totalSlots =
                examPeriod.getTotalDays() * examPeriod.getSlotsPerDay();

        int totalExams = regularExamCount + fixedExamCount;

        if (totalExams > totalSlots) {
            throw new IllegalStateException(
                    "IMPOSSIBLE: Not enough slots. Exams: "
                            + totalExams + ", Slots: " + totalSlots
            );
        }
    }
    // Checks whether at least one classroom can accommodate
    // the student count of every course (impossible otherwise)
    private void checkClassroomCapacity(
            List<Course> courses,
            List<Classroom> classrooms
    ) {
        int maxCapacity = classrooms.stream()
                .mapToInt(Classroom::getCapacity)
                .max()
                .orElse(0);

        for (Course c : courses) {
            if (c.getEnrolledStudents().size() > maxCapacity) {
                throw new IllegalStateException(
                        "IMPOSSIBLE: Course " + c.getCourseCode()
                                + " exceeds all classroom capacities."
                );
            }
        }
    }
}
