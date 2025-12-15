package Desktop_Application_Project_.service;

import Desktop_Application_Project_.ExamPeriod;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Classroom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExamSchedulerService {

    private final ConflictChecker conflictChecker = new ConflictChecker();


    public List<Course> scheduleRegularExams(
            List<Course> courses,
            List<Classroom> classrooms,
            ExamPeriod examPeriod
    ) {

        List<Course> unplacedCourses = new ArrayList<>();

        // 1: Prioritize courses (more students first)
        courses.sort(
                Comparator.comparingInt(
                        (Course c) -> c.getEnrolledStudents().size()
                ).reversed()
        );

        // 2: Try to place each course
        for (Course course : courses) {

            int studentCount = course.getEnrolledStudents().size();
            Placement bestPlacement = null;

            // Scan all days & slots
            for (int day = 1; day <= examPeriod.getTotalDays(); day++) {
                for (int slot = 1; slot <= examPeriod.getSlotsPerDay(); slot++) {

                    // Skip occupied slots (fixed or regular)
                    if (examPeriod.getExamMatrix()[day - 1][slot - 1] != null) {
                        continue;
                    }

                    // Student conflict check
                    boolean conflict =
                            conflictChecker.hasStudentConflict(
                                    course, day, slot, examPeriod, courses
                            );

                    if (conflict) continue;

                    // Scan classrooms
                    for (Classroom classroom : classrooms) {

                        if (!conflictChecker.isRoomCapacityOk(classroom, studentCount)) {
                            continue;
                        }

                        // Simple heuristic scoring
                        int score = calculateScore(day, slot, examPeriod);

                        if (bestPlacement == null || score > bestPlacement.score) {
                            bestPlacement = new Placement(day, slot, score);
                        }
                    }
                }
            }

            // Place course if possible
            if (bestPlacement != null) {
                examPeriod.assignExam(
                        bestPlacement.day - 1,
                        bestPlacement.slot - 1,
                        course.getCourseCode()
                );
            } else {
                unplacedCourses.add(course);
                System.out.println(
                        "Could not place exam for course: " + course.getCourseCode()
                );
            }
        }

        return unplacedCourses;
    }

    private int calculateScore(int day, int slot, ExamPeriod examPeriod) {
        int score = 0;
        score += (examPeriod.getTotalDays() - day) * 10;
        score += (examPeriod.getSlotsPerDay() - slot) * 5;
        return score;
    }


    private static class Placement {
        int day;
        int slot;
        int score;

        Placement(int day, int slot, int score) {
            this.day = day;
            this.slot = slot;
            this.score = score;
        }
    }
}