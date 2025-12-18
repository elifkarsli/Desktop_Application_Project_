package Desktop_Application_Project_.parser.impl;

import Desktop_Application_Project_.exception.DataImportException; // Added underscore
import Desktop_Application_Project_.model.DomainModels.*; // Added underscore
import Desktop_Application_Project_.parser.Parser; // Added underscore

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreParsers {
    
    private static final Logger LOGGER = Logger.getLogger(CoreParsers.class.getName());

    public static class StudentParser implements Parser<Student> {
        @Override
        public List<Student> parse(File file) throws DataImportException {
            List<Student> students = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isHeader = true;
                while ((line = br.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }
                    if (line.trim().isEmpty()) continue;
                    try {
                        students.add(new Student(line));
                    } catch (IllegalArgumentException e) {
                        LOGGER.log(Level.WARNING, "Skipping invalid student record: {0}", line);
                    }
                }
            } catch (IOException e) {
                throw new DataImportException("Failed to read Student file: " + file.getName(), e);
            }
            return students;
        }
    }

    public static class CourseParser implements Parser<Course> {
        @Override
        public List<Course> parse(File file) throws DataImportException {
            List<Course> courses = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isHeader = true;
                while ((line = br.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }
                    if (line.trim().isEmpty()) continue;
                    courses.add(new Course(line));
                }
            } catch (IOException e) {
                throw new DataImportException("Failed to read Course file: " + file.getName(), e);
            }
            return courses;
        }
    }

    public static class ClassroomParser implements Parser<Classroom> {
        private static final String DELIMITER = ";";

        @Override
        public List<Classroom> parse(File file) throws DataImportException {
            List<Classroom> classrooms = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isHeader = true;
                while ((line = br.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(DELIMITER);
                    if (parts.length < 2) {
                        LOGGER.log(Level.WARNING, "Malformed classroom line: {0}", line);
                        continue;
                    }
                    try {
                        classrooms.add(new Classroom(parts[0], Integer.parseInt(parts[1].trim())));
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "Invalid capacity format: {0}", line);
                    }
                }
            } catch (IOException e) {
                throw new DataImportException("Failed to read Classroom file.", e);
            }
            return classrooms;
        }
    }

    public static class AttendanceParser implements Parser<String[]> {

        @Override
        public List<String[]> parse(File file) throws DataImportException {
            List<String[]> rows = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                String currentCourse = null;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;


                    if (!line.startsWith("[")) {
                        currentCourse = line;
                    }

                    else if (currentCourse != null) {
                        String clean = line.replace("[", "").replace("]", "");
                        String[] tokens = clean.split(",");

                        for (String token : tokens) {
                            String studentId =
                                    token.trim().replaceAll("^['\"]|['\"]$", "");

                            rows.add(new String[]{studentId, currentCourse});
                        }

                        currentCourse = null;
                    }
                }

            } catch (Exception e) {
                throw new DataImportException("Failed to parse attendance file", e);
            }

            return rows;
        }
    }


    // service.FixedExam Parser
    public static class FixedExamParser implements Parser<FixedExam> {

        private static final String DELIMITER = ",";

        @Override
        public List<FixedExam> parse(File file) throws DataImportException {
            List<FixedExam> fixedExams = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isHeader = true;
                while ((line = br.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(DELIMITER);
                    if (parts.length < 4) {
                        Logger.getLogger(CoreParsers.class.getName())
                                .log(Level.WARNING, "Malformed fixed exam line: {0}", line);
                        continue;
                    }

                    try {
                        String courseCode = parts[0].trim();
                        int day = Integer.parseInt(parts[1].trim());
                        int slot = Integer.parseInt(parts[2].trim());
                        String classroom = parts[3].trim();

                        fixedExams.add(new FixedExam(courseCode, day, slot, classroom));
                    } catch (NumberFormatException e) {
                        Logger.getLogger(CoreParsers.class.getName())
                                .log(Level.WARNING, "Invalid day/slot format: {0}", line);
                    }
                }
            } catch (IOException e) {
                throw new DataImportException("Failed to read service.FixedExam file: " + file.getName(), e);
            }
            return fixedExams;
        }
    }
}