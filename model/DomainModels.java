package Desktop_Application_Project_.model; // Added underscore

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
}