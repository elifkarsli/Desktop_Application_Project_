package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.model.DomainModels.ExamPeriod;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamSchedulerService {

    private final ConflictChecker conflictChecker = new ConflictChecker();

    // Optimization status
    private Map<Integer, Integer> examsPerDay;
    private Map<Classroom, Integer> classroomUsage;

    public List<Course> scheduleRegularExams(
            List<Course> courses,
            List<Classroom> classrooms,
            ExamPeriod examPeriod
    ) {

        List<Course> unplacedCourses = new ArrayList<>();

        // Initial optimization map
        examsPerDay = new HashMap<>();
        classroomUsage = new HashMap<>();

        for (int day = 1; day <= examPeriod.getTotalDays(); day++) {
            examsPerDay.put(day, 0);
        }

        for (Classroom classroom : classrooms) {
            classroomUsage.put(classroom, 0);
        }
        registerFixedExams(examPeriod);
        // 1: Prioritize courses (more students first)
        courses.sort(
                Comparator.comparingInt(
                        (Course c) -> c.getEnrolledStudents().size()
                ).reversed()
        );

        // 2: Try to place each course
        for (Course course : courses) {


            if (examPeriod.isCourseAlreadyScheduled(course.getCourseCode())) {
                continue;
            }

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

                        // Optimized score
                        int score = calculateScore(day, slot, examPeriod, classroom);

                        if (bestPlacement == null || score > bestPlacement.score) {
                            bestPlacement = new Placement(day, slot, score, classroom);
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

                // Update optimization state
                examsPerDay.put(
                        bestPlacement.day,
                        examsPerDay.get(bestPlacement.day) + 1
                );

                classroomUsage.put(
                        bestPlacement.classroom,
                        classroomUsage.get(bestPlacement.classroom) + 1
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

    // Enhanced score funtion
    private int calculateScore(
            int day,
            int slot,
            ExamPeriod examPeriod,
            Classroom classroom
    ) {

        int score = 0;

        // Original heuristic (KEPT, not removed)
        score += (examPeriod.getTotalDays() - day) * 10;
        score += (examPeriod.getSlotsPerDay() - slot) * 5;


        // Day balance
        score -= examsPerDay.get(day) * 15;

        // Last day penalty
        if (day == examPeriod.getTotalDays()) {
            score -= 50;
        }

        // Classroom balance
        score -= classroomUsage.get(classroom) * 10;

        return score;
    }

    // Internal placement class
    private static class Placement {
        int day;
        int slot;
        int score;
        Classroom classroom;

        Placement(int day, int slot, int score, Classroom classroom) {
            this.day = day;
            this.slot = slot;
            this.score = score;
            this.classroom = classroom;
        }
    }
    private void registerFixedExams(ExamPeriod examPeriod) {

        String[][] matrix = examPeriod.getExamMatrix();

        for (int day = 0; day < matrix.length; day++) {
            for (int slot = 0; slot < matrix[day].length; slot++) {

                if (matrix[day][slot] != null &&
                        matrix[day][slot].startsWith("[FIXED]")) {

                    int realDay = day + 1;
                    examsPerDay.put(realDay, examsPerDay.get(realDay) + 1);
                }
            }
        }
    }

}
