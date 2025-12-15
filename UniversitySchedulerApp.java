package Desktop_Application_Project_;

import Desktop_Application_Project_.exception.DataImportException;
import Desktop_Application_Project_.gui.SchedulerGUI;
import Desktop_Application_Project_.model.DomainModels;
import Desktop_Application_Project_.model.DomainModels.Student;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.ExamPeriod;
import Desktop_Application_Project_.parser.Parser;
import Desktop_Application_Project_.parser.impl.CoreParsers;
import Desktop_Application_Project_.service.*;


import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class UniversitySchedulerApp {

    public static void main(String[] args) {
        System.out.println("--- Starting University Exam Scheduler Import ---");

        File studentFile = new File("CSV_Files/students.csv");
        File courseFile  = new File("CSV_Files/courses.csv");
        File classroomFile    = new File("CSV_Files/classrooms.csv");
        File attendanceFile  = new File("CSV_Files/attendance.csv");

        String outputPath = "output/result.txt";

        try {
            // [1] Parse Students
            System.out.println("\n[1] Parsing Students...");
            List<Student> students = Collections.emptyList();
            if (studentFile.exists()) {
                Parser<Student> studentParser = new CoreParsers.StudentParser();
                students = studentParser.parse(studentFile);
                System.out.println("    Successfully loaded " + students.size() + " students.");
            } else {
                System.out.println("    Student file not found at: " + studentFile.getPath());
            }

            // [2] Parse Classrooms
            System.out.println("\n[2] Parsing Classrooms...");
            List<Classroom> classrooms = Collections.emptyList();
            if (classroomFile.exists()) {
                Parser<Classroom> roomParser = new CoreParsers.ClassroomParser();
                classrooms = roomParser.parse(classroomFile);
                System.out.println("    Successfully loaded " + classrooms.size() + " classrooms.");
            } else {
                System.out.println("    Classroom file not found.");
            }

            // [3] Parse Course List
            System.out.println("\n[3] Parsing Master Course List...");
            List<Course> masterCourses = Collections.emptyList();
            if (courseFile.exists()) {
                Parser<Course> courseParser = new CoreParsers.CourseParser();
                masterCourses = courseParser.parse(courseFile);
                System.out.println("    Successfully loaded " + masterCourses.size() + " master courses.");
            } else {
                System.out.println("    Master Course file not found.");
            }

            // [4] Parse Attendance
            System.out.println("\n[4] Parsing Attendances...");
            List<Course> enrolledCourses = Collections.emptyList();
            if (attendanceFile.exists()) {
                Parser<Course> attendanceParser = new CoreParsers.AttendanceParser();
                enrolledCourses = attendanceParser.parse(attendanceFile);
                System.out.println("    Successfully loaded attendance for " + enrolledCourses.size() + " courses.");
            } else {
                System.out.println("    Attendance file not found.");
            }

            // [5] Data Validation
            System.out.println("\n[5] Validating Referential Integrity...");
            DataValidator validator = new DataValidator();
            List<String> errors = validator.validate(students, classrooms, masterCourses, enrolledCourses);

            if (errors.isEmpty()) {
                System.out.println("    SUCCESS: All data is valid.");

                // [6] Create ExamPeriod configuration (FR3)
                int totalDays = 5;      
                int slotsPerDay = 4;    

                ExamPeriod examPeriod = new ExamPeriod(totalDays, slotsPerDay);
                System.out.println("\n[6] ExamPeriod created: " + totalDays + " Days, " + slotsPerDay + " Slots.");

                // [6a] Fixed Exams logic
                File fixedExamFile = new File("D:\\Desktop_Application_Project\\CSV_Files\\fixed_exams.csv");
                FixedExamService fixedExamService = new FixedExamService();


                if (fixedExamFile.exists()) {
                    System.out.println("    Processing Fixed Exams...");
                    Parser<DomainModels.FixedExam> fixedExamParser = new CoreParsers.FixedExamParser();
                    List<DomainModels.FixedExam> fixedExams = fixedExamParser.parse(fixedExamFile);

                    for (DomainModels.FixedExam fx : fixedExams) {
                        try {
                            fixedExamService.addFixedExam(fx); // conflict check
                            
                            // Assign to matrix (Day - 1 because CSV is usually 1-based)
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

                // [6b] Assigning Regular Exams using improved scheduling algorithm
                System.out.println("\n[6b] Assigning Regular Exams (Improved Algorithm)...");

                ExamSchedulerService schedulerService = new ExamSchedulerService();

                List<Course> unplacedCourses =
                        schedulerService.scheduleRegularExams(
                                enrolledCourses,
                                classrooms,
                                examPeriod
                        );

                if (!unplacedCourses.isEmpty()) {
                    System.out.println("\nUnplaced courses:");
                    for (Course c : unplacedCourses) {
                        System.out.println(" - " + c.getCourseCode());
                    }
                }

                examPeriod.printExamSchedule();

                // [7] Write Output
                System.out.println("\n[7] Writing Final Output...");
                FinalWriter writer = new FinalWriter();
                writer.writeOutput(classrooms, students, masterCourses, enrolledCourses, outputPath);
                
                System.out.println("--- Execution Finished Successfully ---");

                // Launch GUI
                System.out.println("\n[8] Launching GUI...");

                List<Student> finalStudents = students;
                List<Classroom> finalClassrooms = classrooms;
                List<Course> finalMasterCourses = masterCourses;
                List<Course> finalEnrolledCourses = enrolledCourses;

                SwingUtilities.invokeLater(() -> {
                    SchedulerGUI gui = new SchedulerGUI(
                            finalStudents,
                            finalClassrooms,
                            finalMasterCourses,
                            finalEnrolledCourses
                    );
                    gui.setVisible(true);
                });

            } else {
                System.out.println("    WARNING: Found " + errors.size() + " inconsistencies. Files will NOT be written.");
                errors.forEach(e -> System.out.println("      - " + e));
            }

        } catch (DataImportException e) {
            System.err.println("Error importing data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}