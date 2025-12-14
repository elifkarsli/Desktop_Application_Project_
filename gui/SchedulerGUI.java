package Desktop_Application_Project_.gui;
import  Desktop_Application_Project_.model.DomainModels.Classroom;
import  Desktop_Application_Project_.model.DomainModels.Course;
import  Desktop_Application_Project_.model.DomainModels.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulerGUI extends JFrame {

    public SchedulerGUI(List<Student> students,
                        List<Classroom> classrooms,
                        List<Course> masterCourses,
                        List<Course> enrolledCourses) {

        setTitle("University Scheduler - Data Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Tab 1: Students ---
        tabbedPane.addTab("Students (" + students.size() + ")", createStudentPanel(students));

        // --- Tab 2: Classrooms ---
        tabbedPane.addTab("Classrooms (" + classrooms.size() + ")", createClassroomPanel(classrooms));

        // --- Tab 3: Master Courses ---
        tabbedPane.addTab("Master Courses (" + masterCourses.size() + ")", createMasterCoursePanel(masterCourses));

        // --- Tab 4: Enrollments ---
        tabbedPane.addTab("Enrollments (" + enrolledCourses.size() + ")", createEnrollmentPanel(enrolledCourses));

        add(tabbedPane);
    }

    private JPanel createStudentPanel(List<Student> students) {
        String[] columnNames = {"Student ID"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (Student s : students) {
            model.addRow(new Object[]{s.getId()});
        }

        JTable table = new JTable(model);
        return createTablePanel(table);
    }

    private JPanel createClassroomPanel(List<Classroom> classrooms) {
        String[] columnNames = {"Classroom Name", "Capacity"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (Classroom c : classrooms) {
            model.addRow(new Object[]{c.getName(), c.getCapacity()});
        }

        JTable table = new JTable(model);
        return createTablePanel(table);
    }

    private JPanel createMasterCoursePanel(List<Course> courses) {
        String[] columnNames = {"Course Code"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (Course c : courses) {
            model.addRow(new Object[]{c.getCourseCode()});
        }

        JTable table = new JTable(model);
        return createTablePanel(table);
    }

    private JPanel createEnrollmentPanel(List<Course> courses) {
        String[] columnNames = {"Course Code", "Enrolled Count", "Student Sample"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (Course c : courses) {
            // Create a preview string of first 5 students
            String sample = c.getEnrolledStudents().stream()
                    .limit(5)
                    .map(Student::getId)
                    .collect(Collectors.joining(", ")) + (c.getEnrolledStudents().size() > 5 ? "..." : "");

            model.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getEnrolledStudents().size(),
                    sample
            });
        }

        JTable table = new JTable(model);
        // Make the sample column wider
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        return createTablePanel(table);
    }

    private JPanel createTablePanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        table.setAutoCreateRowSorter(true); // Allow sorting by clicking headers
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}