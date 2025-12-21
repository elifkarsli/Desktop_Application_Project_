package Desktop_Application_Project_;

import Desktop_Application_Project_.exception.DataImportException;
import Desktop_Application_Project_.gui.SchedulerGUI;
import Desktop_Application_Project_.model.DomainModels.Student;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.model.DomainModels.FixedExam;
import Desktop_Application_Project_.model.DomainModels.ExamPeriod;
import Desktop_Application_Project_.parser.Parser;
import Desktop_Application_Project_.parser.impl.CoreParsers;
import Desktop_Application_Project_.service.*;
import Desktop_Application_Project_.service.DatabaseManager;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class UniversitySchedulerApp {

    public static void main(String[] args) {
        System.out.println("--- Starting University Exam Scheduler Import ---");

        DatabaseManager.initializeDatabase();

        File studentFile = new File("CSV_Files/students.csv");
        File courseFile  = new File("CSV_Files/courses.csv");
        File classroomFile = new File("CSV_Files/classrooms.csv");
        File attendanceFile = new File("CSV_Files/attendance.csv");
        File fixedExamFile = new File("CSV_Files/fixed_exams.csv");

        String outputPath = "output/result.txt";

        // --- 1. Initialize Lists Empty (Crucial for GUI safety) ---
        List<Student> students = Collections.emptyList();
        List<Classroom> classrooms = Collections.emptyList();
        List<Course> masterCourses = Collections.emptyList();
        List<FixedExam> fixedExams = Collections.emptyList();
        // This list will hold the courses we actually pass to the GUI
        List<Course> coursesForGui = Collections.emptyList();

        try {
            StudentDAO studentDAO = new StudentDAO();
            CourseDAO courseDAO = new CourseDAO();
            ClassroomDAO classroomDAO = new ClassroomDAO();
            AttendanceDAO attendanceDAO = new AttendanceDAO();

            // [1] Parse Students
            System.out.println("\n[1] Parsing Students...");
            Parser<Student> studentParser = new CoreParsers.StudentParser();
            if (studentFile.exists()) {
                if (studentDAO.isEmpty()) {
                    System.out.println("    First time import of students from CSV...");
                    students = studentParser.parse(studentFile);
                    studentDAO.insertStudents(students);
                } else {
                    System.out.println("    Loading students from database...");
                    students = studentDAO.getAllStudents();
                }
            } else {
                System.out.println("    Student file not found at: " + studentFile.getPath());
            }

            // [2] Parse Classrooms
            System.out.println("\n[2] Parsing Classrooms...");
            Parser<Classroom> roomParser = new CoreParsers.ClassroomParser();
            if (classroomFile.exists()) {
                if (classroomDAO.isEmpty()) {
                    System.out.println("    Importing classrooms from CSV (first time)...");
                    classrooms = roomParser.parse(classroomFile);
                    classroomDAO.insertClassrooms(classrooms);
                } else {
                    System.out.println("    Loading classrooms from database...");
                    classrooms = classroomDAO.getAllClassrooms();
                }
                System.out.println("    Successfully loaded " + classrooms.size() + " classrooms.");
            } else {
                System.out.println("    Classroom file not found.");
            }

            // [3] Parse Course List
            System.out.println("\n[3] Parsing Master Course List...");
            Parser<Course> courseParser = new CoreParsers.CourseParser();
            if (courseFile.exists()) {
                if (courseDAO.isEmpty()) {
                    System.out.println("    Importing courses from CSV (first time)...");
                    masterCourses = courseParser.parse(courseFile);
                    courseDAO.insertCourses(masterCourses);
                } else {
                    System.out.println("    Loading courses from database...");
                    masterCourses = courseDAO.getAllCourses();
                }
                System.out.println("    Successfully loaded " + masterCourses.size() + " master courses.");
            } else {
                System.out.println("    Master Course file not found.");
            }

            // Default gui list to master list (will be filtered later if scheduling runs)
            coursesForGui = masterCourses;

            // [4] Process Attendance
            System.out.println("\n[4] Processing Attendances...");
            CourseEnrollmentService enrollmentService = new CourseEnrollmentService();
            if (attendanceFile.exists()) {
                if (attendanceDAO.isEmpty()) {
                    System.out.println("    Importing attendance from CSV (first time)...");
                    Parser<String[]> attendanceParser = new CoreParsers.AttendanceParser();
                    List<String[]> attendanceRows = attendanceParser.parse(attendanceFile);
                    attendanceDAO.insertAttendance(attendanceRows);
                } else {
                    System.out.println("    Loading attendance from database...");
                }
                enrollmentService.loadEnrollments(masterCourses, students);
                System.out.println("    Attendance relations loaded successfully.");
            } else {
                System.out.println("    Attendance file not found.");
            }

            // [5] Data Validation
            System.out.println("\n[5] Validating Referential Integrity...");
            DataValidator validator = new DataValidator();
            List<String> errors = validator.validate(students, classrooms, masterCourses, masterCourses);

            boolean proceedWithScheduling = true;

            if (!errors.isEmpty()) {
                System.out.println("    WARNING: Found " + errors.size() + " inconsistencies.");

                // Show Popup Warning
                String msg = "Data inconsistencies found:\n" + errors.get(0) + "\n\n"
                        + "The application will open, but scheduling may be skipped/incomplete.";
                JOptionPane.showMessageDialog(null, msg, "Data Warning", JOptionPane.WARNING_MESSAGE);

                // Skip scheduling to prevent crashes, but allow GUI to open
                proceedWithScheduling = false;
                System.out.println("    Skipping automated scheduling due to data errors.");
                errors.forEach(e -> System.out.println("      - " + e));
            } else {
                System.out.println("    SUCCESS: All data is valid.");
            }

            // [6] Scheduling Logic (Only run if data is valid)
            if (proceedWithScheduling) {
                // [6] Create ExamPeriod
                int totalDays = 5;
                int slotsPerDay = 4;
                ExamPeriod examPeriod = new ExamPeriod(totalDays, slotsPerDay);
                System.out.println("\n[6] ExamPeriod created: " + totalDays + " Days, " + slotsPerDay + " Slots.");

                // [6a] Fixed Exams logic
                FixedExamService fixedExamService = new FixedExamService();
                if (fixedExamFile.exists()) {
                    System.out.println("    Processing Fixed Exams...");
                    Parser<FixedExam> fixedExamParser = new CoreParsers.FixedExamParser();
                    fixedExams = fixedExamParser.parse(fixedExamFile);

                    for (FixedExam fx : fixedExams) {
                        try {
                            fixedExamService.addFixedExam(fx);
                            boolean assigned = examPeriod.assignFixedExam(fx.getDay() - 1, fx.getSlot() - 1, fx.getCourseCode());
                            if (!assigned) {
                                System.out.println("    WARNING: Slot occupied or out of bounds for " + fx);
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("    Conflict detected: " + e.getMessage());
                        }
                    }
                    examPeriod.printExamSchedule();
                } else {
                    System.out.println("    No fixed exam file found. Skipping.");
                }

                Set<String> fixedCourseCodes = new HashSet<>();
                for (FixedExam fx : fixedExams) {
                    fixedCourseCodes.add(fx.getCourseCode());
                }

                // [6a.5] Impossible Situation Analysis
                System.out.println("\n[6a.5] Checking impossible situations...");
                ImpossibleSituationChecker checker = new ImpossibleSituationChecker();
                checker.check(examPeriod, masterCourses, classrooms, fixedExams.size());
                System.out.println("    Impossible situation check passed.");

                // [6b] Assigning Regular Exams
                System.out.println("\n[6b] Assigning Regular Exams (Improved Algorithm)...");
                ExamSchedulerService schedulerService = new ExamSchedulerService();

                // Filter out fixed exams
                List<Course> regularCourses = new ArrayList<>();
                for (Course c : masterCourses) {
                    if (!fixedCourseCodes.contains(c.getCourseCode())) {
                        regularCourses.add(c);
                    }
                }

                // Update the list for GUI to show filtered regular courses
                coursesForGui = regularCourses;

                List<Course> unplacedCourses = schedulerService.scheduleRegularExams(
                        regularCourses,
                        classrooms,
                        examPeriod
                );

                if (!unplacedCourses.isEmpty()) {
                    System.out.println("\nUnplaced courses:");
                    for (Course c : unplacedCourses) {
                        System.out.println(" - " + c.getCourseCode());
                    }
                    System.out.println("\n[!] Triggering Suggestion Engine...");
                    SuggestionEngine suggestionEngine = new SuggestionEngine();
                    suggestionEngine.analyzeAndSuggest(masterCourses, classrooms, fixedExams, totalDays, slotsPerDay);
                }

                examPeriod.printExamSchedule();

                // [7] Write Output
                System.out.println("\n[7] Writing Final Output...");
                FinalWriter writer = new FinalWriter();
                writer.writeOutput(classrooms, students, masterCourses, masterCourses, outputPath);

                System.out.println("--- Execution Finished Successfully ---");
            }

            // [8] Launch GUI
            System.out.println("\n[8] Launching GUI...");

            final List<Student> finalStudents = students;
            final List<Classroom> finalClassrooms = classrooms;
            final List<Course> finalCourses = coursesForGui;
            final List<FixedExam> finalFixedExams = fixedExams;

            SwingUtilities.invokeLater(() -> {
                SchedulerGUI gui = new SchedulerGUI(
                        finalStudents,
                        finalClassrooms,
                        finalCourses,
                        finalCourses, // passed as enrolled courses
                        finalFixedExams
                );
                gui.setVisible(true);
            });

        } catch (DataImportException e) {
            System.err.println("Error importing data: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Data Import Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Critical Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}