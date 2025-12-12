package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Student;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Classroom;

import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataValidator {

    public List<String> validate(List<Student> students,
                                 List<Classroom> classrooms,
                                 List<Course> masterCourses,
                                 List<Course> enrolledCourses) {

        List<String> errors = new ArrayList<>();

        try {
            validateStudents(students);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        try {
            validateCourses(masterCourses);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        try {
            validateClassrooms(classrooms);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        try {
            // We pass the "Enrolled Courses" as the Attendance data
            validateCourseRegistrations(enrolledCourses, students, masterCourses, classrooms);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        return errors;
    }


    // 1. Student validation
    private void validateStudents(List<Student> students) {
        Set<String> seen = new HashSet<>();

        for (Student s : students) {
            String studentId = s.getId();

            if (studentId == null || studentId.isBlank()) {
                throw new IllegalArgumentException("ERROR: Student ID cannot be empty!");
            }

            if (!studentId.startsWith("Std_ID_")) {
                throw new IllegalArgumentException("ERROR: Invalid student format: " + studentId);
            }

            if (seen.contains(studentId)) {
                throw new IllegalArgumentException("ERROR: Duplicate student ID: " + studentId);
            }

            seen.add(studentId);
        }
    }

    // 2. Course validation
    private void validateCourses(List<Course> courses) {
        Set<String> seen = new HashSet<>();

        for (Course c : courses) {
            String code = c.getCourseCode();

            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("ERROR: Course code cannot be empty!");
            }

            if (!code.startsWith("CourseCode_")) {
                throw new IllegalArgumentException("ERROR: Invalid course code format: " + code);
            }

            if (seen.contains(code)) {
                throw new IllegalArgumentException("ERROR: Duplicate course code: " + code);
            }

            seen.add(code);
        }
    }

    // 3. Classroom validation
    private void validateClassrooms(List<Classroom> classrooms) {
        Set<String> seen = new HashSet<>();

        for (Classroom c : classrooms) {
            String name = c.getName();

            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("ERROR: Classroom code cannot be empty!");
            }

            if (!name.startsWith("Classroom_")) {
                throw new IllegalArgumentException("ERROR: Invalid classroom code format: " + name);
            }

            if (seen.contains(name)) {
                throw new IllegalArgumentException("ERROR: Duplicate classroom code: " + name);
            }

            seen.add(name);

            if (c.getCapacity() <= 0) {
                throw new IllegalArgumentException("ERROR: Classroom capacity must be > 0: " + name);
            }
        }
    }

    // 4. Course registration validation
    private void validateCourseRegistrations(
            List<Course> enrolledCourses, // This represents the Attendance file
            List<Student> allStudents,
            List<Course> allCourses,
            List<Classroom> classrooms
    ) {
        Set<String> studentSet = allStudents.stream().map(Student::getId).collect(Collectors.toSet());
        Set<String> courseSet = allCourses.stream().map(Course::getCourseCode).collect(Collectors.toSet());

        // Map classroom name to capacity
        Map<String, Integer> classroomMap = classrooms.stream()
                .collect(Collectors.toMap(Classroom::getName, Classroom::getCapacity));

        int maxRoomCapacity = 0;
        if (!classroomMap.isEmpty()) {
            maxRoomCapacity = classroomMap.values().stream().max(Integer::compare).orElse(0);
        }

        for (Course enrollment : enrolledCourses) {
            String courseCode = enrollment.getCourseCode();
            List<Student> registeredStudents = enrollment.getEnrolledStudents();

            // Check if course exists in Master List
            if (!courseSet.contains(courseCode)) {
                throw new IllegalArgumentException("ERROR: Registration includes unknown course: " + courseCode);
            }

            if (registeredStudents == null) {
                throw new IllegalArgumentException("ERROR: Course " + courseCode + " has no student list!");
            }

            Set<String> studentInCourse = new HashSet<>();

            for (Student s : registeredStudents) {
                String studentId = s.getId();

                // Check if student exists in Master List
                if (!studentSet.contains(studentId)) {
                    throw new IllegalArgumentException("ERROR: Student " + studentId +
                            " registered to " + courseCode + " but not in student list!");
                }

                // Check for duplicate student in same course
                if (studentInCourse.contains(studentId)) {
                    throw new IllegalArgumentException("ERROR: Duplicate registration for student " + studentId +
                            " in course " + courseCode);
                }
                studentInCourse.add(studentId);
            }

            // Capacity Check
            if (registeredStudents.size() > maxRoomCapacity) {
                throw new IllegalArgumentException("ERROR: Course " + courseCode +
                        " has " + registeredStudents.size() + " students, " +
                        "but max classroom capacity is only " + maxRoomCapacity);
            }
        }
    }
}

