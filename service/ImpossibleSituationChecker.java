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

    // 1️⃣ Slot sayısı yeterli mi?
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

    // 2️⃣ Classroom kapasitesi yeterli mi?
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
