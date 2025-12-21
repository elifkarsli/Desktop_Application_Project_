package Desktop_Application_Project_.model;

import java.util.ArrayList;
import java.util.List;

public class DomainModels {

    /* ===================== STUDENT ===================== */
    public static class Student {
        private final String id;

        public Student(String id) {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Student ID cannot be null or empty");
            }
            this.id = id.trim();
        }

        public String getId() { return id; }
    }

    /* ===================== COURSE ===================== */
    public static class Course {
        private final String courseCode;
        private final List<Student> enrolledStudents = new ArrayList<>();

        public Course(String courseCode) {
            if (courseCode == null || courseCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Course Code cannot be null or empty");
            }
            this.courseCode = courseCode.trim();
        }
        public void enrollStudent(Student student) {
            if (student != null) {
                enrolledStudents.add(student);
            }
        }


        public String getCourseCode() { return courseCode; }
        public List<Student> getEnrolledStudents() { return enrolledStudents; }
    }

    /* ===================== CLASSROOM ===================== */
    public static class Classroom {
        private final String name;
        private final int capacity;

        public Classroom(String name, int capacity) {
            this.name = name.trim();
            this.capacity = capacity;
        }

        public String getName() { return name; }
        public int getCapacity() { return capacity; }
    }

    /* ===================== EXAM PERIOD ===================== */
    public static class ExamPeriod {

        private final int totalDays;
        private final int slotsPerDay;
        private final String[][] examMatrix;

        public ExamPeriod(int totalDays, int slotsPerDay) {
            this.totalDays = totalDays;
            this.slotsPerDay = slotsPerDay;
            this.examMatrix = new String[totalDays][slotsPerDay];
        }
        public int getTotalDays() {
            return totalDays;
        }

        public int getSlotsPerDay() {
            return slotsPerDay;
        }



        public boolean isFixedSlot(int day, int slot) {
            return examMatrix[day][slot] != null &&
                    examMatrix[day][slot].startsWith("[FIXED]");
        }

        public boolean isCourseAlreadyScheduled(String courseCode) {
            for (int d = 0; d < totalDays; d++) {
                for (int s = 0; s < slotsPerDay; s++) {
                    if (examMatrix[d][s] != null) {
                        String clean = examMatrix[d][s]
                                .replace("[FIXED]", "")
                                .trim();
                        if (clean.equals(courseCode)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }


        public boolean assignExam(int day, int slot, String courseCode) {


            if (isCourseAlreadyScheduled(courseCode)) {
                return false;
            }


            if (isFixedSlot(day, slot)) {
                return false;
            }


            if (examMatrix[day][slot] != null) {
                return false;
            }

            examMatrix[day][slot] = courseCode;
            return true;
        }


        public boolean assignFixedExam(int day, int slot, String courseCode) {
            if (examMatrix[day][slot] != null) {
                return false;
            }
            examMatrix[day][slot] = "[FIXED] " + courseCode;
            return true;
        }

        public String[][] getExamMatrix() {
            return examMatrix;
        }

        public void printExamSchedule() {
            System.out.println("\n=== FINAL EXAM SCHEDULE ===");
            for (int d = 0; d < totalDays; d++) {
                System.out.print("Day " + (d + 1) + ": ");
                for (int s = 0; s < slotsPerDay; s++) {
                    System.out.print(
                            (examMatrix[d][s] != null ? examMatrix[d][s] : "empty") + " | "
                    );
                }
                System.out.println();
            }
        }
    }

    /* ===================== FIXED EXAM ===================== */
    public static class FixedExam {
        private final String courseCode;
        private final int day;
        private final int slot;
        private final String classroom;

        public FixedExam(String courseCode, int day, int slot, String classroom) {
            this.courseCode = courseCode;
            this.day = day;
            this.slot = slot;
            this.classroom = classroom;
        }

        public String getCourseCode() { return courseCode; }
        public int getDay() { return day; }
        public int getSlot() { return slot; }
        public String getClassroom() { return classroom; }
    }
}

