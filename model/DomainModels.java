package Desktop_Application_Project_.model;

import java.util.ArrayList;
import java.util.List;

public class DomainModels {

    public static class Student {
        private final String id;

        public Student(String id) {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Student ID cannot be null or empty");
            }
            this.id = id.trim();
        }

        public String getId() { return id; }

        @Override
        public String toString() { return "Student{id='" + id + "'}"; }
    }

    public static class Course {
        private final String courseCode;
        private final List<Student> enrolledStudents;

        public Course(String courseCode) {
            if (courseCode == null || courseCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Course Code cannot be null or empty");
            }
            this.courseCode = courseCode.trim();
            this.enrolledStudents = new ArrayList<>();
        }

        public void enrollStudent(Student student) {
            if (student != null) {
                this.enrolledStudents.add(student);
            }
        }

        public String getCourseCode() { return courseCode; }
        public List<Student> getEnrolledStudents() { return enrolledStudents; }

        @Override
        public String toString() {
            return "Course{code='" + courseCode + "', enrollmentCount=" + enrolledStudents.size() + "}";
        }
    }

    public static class Classroom {
        private final String name;
        private final int capacity;

        public Classroom(String name, int capacity) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Classroom name cannot be empty");
            }
            if (capacity < 0) {
                throw new IllegalArgumentException("Capacity cannot be negative");
            }
            this.name = name.trim();
            this.capacity = capacity;
        }
        public String getName() { return name; }
        public int getCapacity() { return capacity; }

        @Override
        public String toString() { return "Classroom{name='" + name + "', capacity=" + capacity + "}"; }
    }

        //  ExamPeriod entity
        public static class ExamPeriod {
            private final int totalDays;
            private final int slotsPerDay;
            private final String[][] examMatrix;

            public ExamPeriod(int totalDays, int slotsPerDay) {
                if (totalDays <= 0 || slotsPerDay <= 0) {
                    throw new IllegalArgumentException("totalDays and slotsPerDay must be positive");
                }
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

            public String[][] getExamMatrix() {
                return examMatrix;
            }
        
            
        }
        // Fixed Exam entity
        public static class FixedExam {
            private final String courseCode;
            private final int day;
            private final int slot;
            private final String classroom;
            private final boolean locked;

            public FixedExam(String courseCode, int day, int slot, String classroom) {
                this.courseCode = courseCode;
                this.day = day;
                this.slot = slot;
                this.classroom = classroom;
                this.locked = true; // fixed exam is always locked
            }

            public String getCourseCode() { return courseCode; }
            public int getDay() { return day; }
            public int getSlot() { return slot; }
            public String getClassroom() { return classroom; }
            public boolean isLocked() { return locked; }

            @Override
            public String toString() {
                return "[Fixed] " + courseCode + " @ Day " + day + ", Slot " + slot + " (" + classroom + ")";
            }
        }
}