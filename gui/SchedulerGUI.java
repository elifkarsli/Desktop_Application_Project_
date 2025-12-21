package Desktop_Application_Project_.gui;

import Desktop_Application_Project_.service.*;
import Desktop_Application_Project_.model.DomainModels.ExamPeriod;
import Desktop_Application_Project_.exception.DataImportException;
import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.FixedExam;
import Desktop_Application_Project_.model.DomainModels.Student;
import Desktop_Application_Project_.parser.Parser;
import Desktop_Application_Project_.parser.impl.CoreParsers;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SchedulerGUI extends JFrame {

    // Data lists

    private List<Student> students;
    private List<Classroom> classrooms;
    private List<Course> masterCourses;
    private List<Course> enrolledCourses;
    private List<FixedExam> fixedExams;
    private JComboBox<String> courseSelector;
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final ClassroomDAO classroomDAO = new ClassroomDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final CourseEnrollmentService enrollmentService = new CourseEnrollmentService();

    // Scheduling Objects
    private ExamPeriod examPeriod;


    // Navigation Buttons
    private JButton btnDashboard, btnImport, btnValidate, btnConfig, btnScheduler, btnResults, btnTimetable;

    // Main Content Area (CardLayout for switching screens)
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // UI Components for updates
    private JLabel lblStudentCount, lblClassroomCount, lblCourseCount, lblAttendanceCount;
    private JTable resultsTable;
    private JTable visualTimetable; // Visual Timetable Table
    private JSpinner spinDays, spinSlots;
    private JTextPane validationLogArea; // Changed from JTextArea to JTextPane for HTML
    private JTextField txtSearch; // Search Bar

    // Scheduler UI refs (for toggling)
    private JLabel lblScheduledCount;
    private JButton btnViewResults;
    private JPanel schedulerStatusPanel;



    // DESIGN SYSTEM CONSTANTS
    // "Linear" / "Vercel" inspired palette
    private final Color BG_CANVAS = new Color(248, 250, 252); // #F8FAFC
    private final Color BG_CARD = Color.WHITE;                // #FFFFFF
    private final Color SIDEBAR_COLOR = new Color(30, 41, 59); // #1E293B (Deep Slate)
    private final Color ACCENT_BLUE = new Color(59, 130, 246); // #3B82F6 (Electric Blue)
    private final Color TEXT_PRIMARY = new Color(15, 23, 42);  // Dark Slate
    private final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate Gray
    private final Color BORDER_COLOR = new Color(226, 232, 240);   // Light Gray Border
    private final Color SUCCESS_GREEN = new Color(34, 197, 94);    // Pastel Green
    private final Color ERROR_RED = new Color(239, 68, 68);        // Pastel Red

    // Fonts for GUI
    private final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 24);
    private final Font FONT_SUBHEADER = new Font("SansSerif", Font.BOLD, 14);
    private final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    private final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 13);

    private Timer activeAnimTimer;
    private int currentInset = 20;
    private final int TARGET_INSET = 28;


    private final Border MENU_NORMAL_BORDER =
            new EmptyBorder(10, 20, 10, 15);

    private final Border MENU_ACTIVE_BORDER =
            BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT_BLUE),
                    new EmptyBorder(10, 24, 10, 15)
            );


    public SchedulerGUI(List<Student> students, List<Classroom> classrooms, List<Course> masterCourses, List<Course> enrolledCourses, List<FixedExam> fixedExams) {
        this.students = students;
        this.classrooms = classrooms;
        this.masterCourses = masterCourses;
        this.enrolledCourses = enrolledCourses;
        this.fixedExams = fixedExams;
        // Default Configuration
        this.examPeriod = new ExamPeriod(5, 4);
        loadFromDatabaseIfAvailable();

        // Basic Frame Setup
        setTitle("University Exam Scheduler System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(BG_CANVAS);

        // 1. Create Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Create Top Bar
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // 3. Create Main Content Area
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_CANVAS);

        // Add Screens (Cards)
        mainContentPanel.add(createDashboardPanel(), "dashboard");
        mainContentPanel.add(createImportPanel(), "import");
        mainContentPanel.add(createValidatePanel(), "validate");
        mainContentPanel.add(createConfigPanel(), "config");
        mainContentPanel.add(createSchedulerPanel(), "scheduler");
        mainContentPanel.add(createResultsPanel(), "results");
        mainContentPanel.add(createTimetablePanel(), "timetable"); // Added Timetable Card

        add(mainContentPanel, BorderLayout.CENTER);

        // Initial Stat Update
        updateStats();
        refreshCourseSelector(courseSelector);
        updateResultsTable();
        updateVisualTimetable();
        updateStats();

        setActiveButton(btnDashboard);

    }

    // Helper Methods to Build UI
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(12, 1, 8, 8)); // More spacing
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel lblTitle = new JLabel("UniScheduler", SwingConstants.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(0, 10, 20, 0));
        sidebar.add(lblTitle);

        btnDashboard = createMenuButton("Dashboard");
        btnImport = createMenuButton("Import Data");
        btnValidate = createMenuButton("Validate Data");
        btnConfig = createMenuButton("Exam Period");
        btnScheduler = createMenuButton("Run Scheduler");
        btnResults = createMenuButton("View Results");
        btnTimetable = createMenuButton("Timetable Grid"); // New Button

        btnDashboard.addActionListener(e -> {
            setActiveButton(btnDashboard);
            cardLayout.show(mainContentPanel, "dashboard");
        });
        btnImport.addActionListener(e -> {
            setActiveButton(btnImport);
            cardLayout.show(mainContentPanel, "import");
        });

        btnValidate.addActionListener(e -> {
            setActiveButton(btnValidate);
            cardLayout.show(mainContentPanel, "validate");
        });

        btnConfig.addActionListener(e -> {
            setActiveButton(btnConfig);
            cardLayout.show(mainContentPanel, "config");
        });

        btnScheduler.addActionListener(e -> {
            setActiveButton(btnScheduler);
            cardLayout.show(mainContentPanel, "scheduler");
        });

        btnResults.addActionListener(e -> {
            setActiveButton(btnResults);
            cardLayout.show(mainContentPanel, "results");
        });

        btnTimetable.addActionListener(e -> {
            setActiveButton(btnTimetable);
            cardLayout.show(mainContentPanel, "timetable");
            updateVisualTimetable();
        });

        sidebar.add(btnDashboard);
        sidebar.add(btnImport);
        sidebar.add(btnValidate);
        sidebar.add(btnConfig);
        sidebar.add(btnScheduler);
        sidebar.add(btnResults);
        sidebar.add(btnTimetable); // Add to sidebar

        // Spacer to push content up
        sidebar.add(Box.createGlue());

        return sidebar;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setIconTextGap(10);
        btn.setFocusPainted(false);
        btn.setBackground(SIDEBAR_COLOR); // Transparent-ish look
        btn.setForeground(new Color(203, 213, 225)); // Lighter text
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(MENU_NORMAL_BORDER);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Simple hover effect logic
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(new Color(51, 65, 85)); // Lighter slate on hover
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(SIDEBAR_COLOR);
                btn.setForeground(new Color(203, 213, 225));
            }
        });

        return btn;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 70));
        topBar.setBackground(BG_CARD);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JLabel lblHeader = new JLabel("    Exam Management Console");
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblHeader.setForeground(TEXT_PRIMARY);

        // Right side container for Help + Status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 18));
        rightPanel.setBackground(BG_CARD);

        // HELP BUTTON
        JButton btnHelp = new JButton("Need Help?");
        btnHelp.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnHelp.setBackground(new Color(241, 245, 249));
        btnHelp.setForeground(ACCENT_BLUE);
        btnHelp.setFocusPainted(false);
        btnHelp.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        btnHelp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHelp.setPreferredSize(new Dimension(110, 32));
        
        btnHelp.addActionListener(e -> showHelpDialog());

        JLabel lblToast = new JLabel("System Ready");
        lblToast.setForeground(SUCCESS_GREEN);
        lblToast.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblToast.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));

        rightPanel.add(btnHelp);
        rightPanel.add(lblToast);

        topBar.add(lblHeader, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    private void showHelpDialog() {
        new TutorialDialog(this).setVisible(true);
    }

    // Inner class for the paginated help dialog
    private class TutorialDialog extends JDialog {
        private int currentIndex = 0;
        private final String[][] tutorials = {
            {"Dashboard", "<html><b>1. Dashboard Overview</b><br><br>The Dashboard provides a high-level summary of your system.<br><br>• <b>Stats:</b> View total counts of loaded Students, Classrooms, and Courses.<br>• <b>Activity Feed:</b> Monitor recent system logs and status updates.<br>• <b>Status:</b> Ensure the system is ready before proceeding.</html>"},
            {"Import Data", "<html><b>2. Import Data</b><br><br>Load your external data files into the system.<br><br>• <b>Drag & Drop:</b> distinct zones for Students, Courses, Classrooms, etc.<br>• <b>CSV Support:</b> Only .csv files are supported.<br>• <b>Action:</b> Click 'Load & Parse' to process the files into memory.</html>"},
            {"Validate Data", "<html><b>3. Validate Data</b><br><br>Check for data consistency and errors.<br><br>• <b>Integrity Check:</b> Scans for orphan records (e.g., student enrolled in non-existent course).<br>• <b>Capacity Check:</b> Verifies if classrooms fit enrolled students.<br>• <b>Feedback:</b> Look for Green (Success) or Red (Error) logs.</html>"},
            {"Exam Period", "<html><b>4. Exam Period Configuration</b><br><br>Define the temporal structure of your exams.<br><br>• <b>Days:</b> Total number of days available for exams.<br>• <b>Slots:</b> Number of time slots per day.<br>• <b>Setup:</b> You must configure this before running the scheduler.</html>"},
            {"Run Scheduler", "<html><b>5. Run Scheduler</b><br><br>The core algorithm engine.<br><br>• <b>Generate:</b> Attempts to assign every course to a time slot and room.<br>• <b>Fixed Exams:</b> Respects pre-assigned fixed exams.<br>• <b>Conflict Handling:</b> If scheduling fails, the system provides suggestions.</html>"},
            {"View Results", "<html><b>6. View Results</b><br><br>Explore the generated schedule in detail.<br><br>• <b>Filters:</b> View by Day, Student, Course, or Classroom.<br>• <b>Search:</b> You can only search for one thing in the search bar and combine them with the tags.<br>• <b>Export:</b> Save the final schedule as a CSV file.</html>"},
            {"Timetable", "<html><b>7. Timetable</b><br><br>A graphical grid representation.<br><br>• <b>Structure:</b> Rows are Time Slots (09:00 start), Columns are Days.<br>• <b>Usage:</b> Great for visually checking the distribution of exams.<br>• <b>Read-Only:</b> This view is updated automatically.</html>"}
        };

        private JLabel lblContent;
        private JLabel lblStep;
        private JButton btnPrev;
        private JButton btnNext;

        public TutorialDialog(Frame owner) {
            super(owner, "Application Tutorial", true);
            setSize(600, 400);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            setResizable(false);

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(248, 250, 252));
            header.setBorder(new EmptyBorder(20, 30, 20, 30));
            
            JLabel lblTitle = new JLabel("User Guide");
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
            lblTitle.setForeground(new Color(15, 23, 42));
            header.add(lblTitle, BorderLayout.WEST);
            
            lblStep = new JLabel("Step 1 of " + tutorials.length);
            lblStep.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblStep.setForeground(new Color(100, 116, 139));
            header.add(lblStep, BorderLayout.EAST);

            add(header, BorderLayout.NORTH);

            // Content
            lblContent = new JLabel();
            lblContent.setFont(new Font("SansSerif", Font.PLAIN, 14));
            lblContent.setVerticalAlignment(SwingConstants.TOP);
            lblContent.setBorder(new EmptyBorder(20, 40, 20, 40));
            add(lblContent, BorderLayout.CENTER);

            // Footer / Controls
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            footer.setBackground(Color.WHITE);
            footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

            btnPrev = new JButton("Previous");
            styleButton(btnPrev, false);
            btnPrev.addActionListener(e -> navigate(-1));

            btnNext = new JButton("Next");
            styleButton(btnNext, true);
            btnNext.addActionListener(e -> navigate(1));

            footer.add(btnPrev);
            footer.add(btnNext);
            add(footer, BorderLayout.SOUTH);

            updateView();
        }

        private void navigate(int direction) {
            currentIndex += direction;
            if (currentIndex < 0) currentIndex = 0;
            if (currentIndex >= tutorials.length) {
                dispose(); // Finish
                return;
            }
            updateView();
        }

        private void updateView() {
            lblContent.setText(tutorials[currentIndex][1]);
            lblStep.setText("Step " + (currentIndex + 1) + " of " + tutorials.length);
            
            btnPrev.setEnabled(currentIndex > 0);
            if (currentIndex == tutorials.length - 1) {
                btnNext.setText("Finish");
                btnNext.setBackground(new Color(34, 197, 94)); // Green
            } else {
                btnNext.setText("Next");
                btnNext.setBackground(new Color(59, 130, 246)); // Blue
            }
        }

        private void styleButton(JButton btn, boolean primary) {
            btn.setFont(new Font("SansSerif", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorder(new EmptyBorder(10, 20, 10, 20));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (primary) {
                btn.setBackground(new Color(59, 130, 246));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(241, 245, 249));
                btn.setForeground(new Color(15, 23, 42));
            }
        }
    }

    // Screen 1: Dashboard Panel
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CANVAS);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_CANVAS);
        JLabel lblWelcome = new JLabel("Dashboard Overview");
        lblWelcome.setFont(FONT_HEADER);
        lblWelcome.setForeground(TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Welcome, here is the current system status.");
        lblSub.setFont(FONT_BODY);
        lblSub.setForeground(TEXT_SECONDARY);
        headerPanel.add(lblWelcome, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Stats Cards Grid
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 25, 0));
        statsPanel.setBackground(BG_CANVAS);
        statsPanel.setBorder(new EmptyBorder(30, 0, 30, 0));

        lblStudentCount = createStatLabel();
        lblClassroomCount = createStatLabel();
        lblCourseCount = createStatLabel();
        lblAttendanceCount = createStatLabel();

        statsPanel.add(createMetricCard("Students", lblStudentCount, "Loaded"));
        statsPanel.add(createMetricCard("Classrooms", lblClassroomCount, "Available"));
        statsPanel.add(createMetricCard("Courses", lblCourseCount, "Added"));
        statsPanel.add(createMetricCard("Records", lblAttendanceCount, "Enrollments"));

        // Activity Feed
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(BG_CARD);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblActivityTitle = new JLabel("Recent Activities");
        lblActivityTitle.setFont(FONT_SUBHEADER);
        lblActivityTitle.setForeground(TEXT_PRIMARY);

        JTextArea activityList = new JTextArea();
        activityList.setText("• System initialized successfully.\n• Waiting for data import...\n• ");
        activityList.setFont(FONT_BODY);
        activityList.setForeground(TEXT_SECONDARY);
        activityList.setEditable(false);
        activityList.setLineWrap(true);
        activityList.setBackground(BG_CARD);

        activityPanel.add(lblActivityTitle, BorderLayout.NORTH);
        activityPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER); // Spacer
        activityPanel.add(activityList, BorderLayout.SOUTH);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(BG_CANVAS);
        centerContainer.add(statsPanel, BorderLayout.NORTH);
        centerContainer.add(activityPanel, BorderLayout.CENTER);

        panel.add(centerContainer, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatLabel() {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    private JPanel createMetricCard(String title, JLabel lblValue, String trend) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_SECONDARY);

        JLabel lblTrend = new JLabel(trend);
        lblTrend.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblTrend.setForeground(ACCENT_BLUE);

        JPanel topPart = new JPanel(new BorderLayout());
        topPart.setBackground(BG_CARD);
        topPart.add(lblTitle, BorderLayout.WEST);

        card.add(topPart, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblTrend, BorderLayout.SOUTH);

        return card;
    }

    private void updateStats() {
        lblStudentCount.setText(String.valueOf(students != null ? students.size() : 0));
        lblClassroomCount.setText(String.valueOf(classrooms != null ? classrooms.size() : 0));
        lblCourseCount.setText(String.valueOf(masterCourses != null ? masterCourses.size() : 0));
        lblAttendanceCount.setText(String.valueOf(enrolledCourses != null ? enrolledCourses.size() : 0));
    }

    // Screen 2: Import Panel (Drag & Drop Zone Design)
    private JPanel createImportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_CANVAS);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        JLabel title = new JLabel("Import Data Files");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // Visual Drag & Drop Zones
        JTextField txtStudent = createDragDropZone(panel, "Students File", "CSV_Files/students.csv", 1, 0);
        JTextField txtCourse = createDragDropZone(panel, "Courses File", "CSV_Files/courses.csv", 1, 1);
        JTextField txtRoom = createDragDropZone(panel, "Classrooms File", "CSV_Files/classrooms.csv", 2, 0);
        JTextField txtAtt = createDragDropZone(panel, "Attendance File", "CSV_Files/attendance.csv", 2, 1);
        JTextField txtFixed = createDragDropZone(panel, "Fixed Exams File", "CSV_Files/fixed_exams.csv", 3, 0);

        JButton btnLoad = new JButton("Load & Parse Files");
        stylePrimaryButton(btnLoad);

        btnLoad.addActionListener(e -> {
            try {

                StudentDAO studentDAO = new StudentDAO();
                CourseDAO courseDAO = new CourseDAO();
                ClassroomDAO classroomDAO = new ClassroomDAO();
                AttendanceDAO attendanceDAO = new AttendanceDAO();

                File studentFile = new File(txtStudent.getText());
                if (studentFile.exists()) {
                    Parser<Student> studentParser = new CoreParsers.StudentParser();
                    students = studentParser.parse(studentFile);

                    studentDAO.clearTable();
                    studentDAO.insertStudents(students);
                } else {
                    students = studentDAO.getAllStudents();
                }

                File courseFile = new File(txtCourse.getText());
                if (courseFile.exists()) {
                    Parser<Course> courseParser = new CoreParsers.CourseParser();
                    masterCourses = courseParser.parse(courseFile);

                    courseDAO.clearTable();
                    courseDAO.insertCourses(masterCourses);
                } else {
                    masterCourses = courseDAO.getAllCourses();
                }

                File roomFile = new File(txtRoom.getText());
                if (roomFile.exists()) {
                    Parser<Classroom> roomParser = new CoreParsers.ClassroomParser();
                    classrooms = roomParser.parse(roomFile);

                    classroomDAO.clearTable();
                    classroomDAO.insertClassrooms(classrooms);
                } else {
                    classrooms = classroomDAO.getAllClassrooms();
                }

                File attFile = new File(txtAtt.getText());
                if (attFile.exists()) {

                    Parser<String[]> attendanceParser = new CoreParsers.AttendanceParser();
                    List<String[]> rows = attendanceParser.parse(attFile);

                    // Clear in-memory enrollments
                    for (Course c : masterCourses) {
                        c.getEnrolledStudents().clear();
                    }

                    // Rebuild relations
                    for (String[] row : rows) {
                        if (row.length < 2) continue;

                        String studentId = row[0];
                        String courseCode = row[1];

                        Optional<Student> studentOpt = students.stream()
                                .filter(s -> s.getId().equals(studentId))
                                .findFirst();

                        Optional<Course> courseOpt = masterCourses.stream()
                                .filter(c -> c.getCourseCode().equals(courseCode))
                                .findFirst();

                        if (studentOpt.isPresent() && courseOpt.isPresent()) {
                            courseOpt.get().enrollStudent(studentOpt.get());
                        }
                    }

                    attendanceDAO.clearTable();

                    List<String[]> attendanceRowsForDB = new ArrayList<>();
                    for (Course c : masterCourses) {
                        for (Student s : c.getEnrolledStudents()) {
                            attendanceRowsForDB.add(
                                    new String[]{s.getId(), c.getCourseCode()}
                            );
                        }
                    }

                    attendanceDAO.insertAttendance(attendanceRowsForDB);
                }

                enrolledCourses = masterCourses;

                File fixedFile = new File(txtFixed.getText());
                if (fixedFile.exists()) {
                    Parser<FixedExam> fixedParser = new CoreParsers.FixedExamParser();
                    fixedExams = fixedParser.parse(fixedFile);
                }

                courseSelector.removeAllItems();
                courseSelector.addItem("Select Course");
                for (Course c : masterCourses) {
                    courseSelector.addItem(c.getCourseCode());
                }

                updateStats();

                JOptionPane.showMessageDialog(
                        this,
                        "Data Loaded Successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (DataImportException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error loading data: " + ex.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(40, 0, 0, 0);
        panel.add(btnLoad, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CANVAS);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JTextField createDragDropZone(JPanel panel, String labelText, String defaultPath, int row, int col) {
        // Container for the zone
        JPanel zone = new JPanel(new BorderLayout());
        zone.setBackground(BG_CARD);
        zone.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Dashed Border Simulation
        zone.setBorder(BorderFactory.createCompoundBorder(
                new DashedBorder(TEXT_SECONDARY), // Custom dashed look
                new EmptyBorder(20, 20, 20, 20)
        ));
        zone.setPreferredSize(new Dimension(300, 100));

        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(TEXT_PRIMARY);

        JTextField txtPath = new JTextField(defaultPath);
        txtPath.setBorder(null);
        txtPath.setBackground(BG_CARD);
        txtPath.setHorizontalAlignment(SwingConstants.CENTER);
        txtPath.setForeground(TEXT_SECONDARY);
        txtPath.setEditable(false);

        JLabel lblIcon = new JLabel("↓ Click to Browse CSV", SwingConstants.CENTER);
        lblIcon.setForeground(ACCENT_BLUE);
        lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 12));

        zone.add(lbl, BorderLayout.NORTH);
        zone.add(txtPath, BorderLayout.CENTER);
        zone.add(lblIcon, BorderLayout.SOUTH);

        // Click Listener for File Chooser
        MouseAdapter openChooser = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
                fileChooser.setFileFilter(filter);

                int result = fileChooser.showOpenDialog(SchedulerGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    txtPath.setText(selectedFile.getAbsolutePath());
                    lblIcon.setText("File Selected: " + selectedFile.getName());
                    lblIcon.setForeground(SUCCESS_GREEN);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                zone.setBackground(new Color(241, 245, 249));
                txtPath.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                zone.setBackground(BG_CARD);
                txtPath.setBackground(BG_CARD);
            }
        };

        zone.addMouseListener(openChooser);
        txtPath.addMouseListener(openChooser);
        lblIcon.addMouseListener(openChooser);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.gridx = col;
        gbc.gridy = row;
        panel.add(zone, gbc);

        return txtPath;
    }

    // Custom Dashed Border Class
    private static class DashedBorder extends LineBorder {
        public DashedBorder(Color color) { super(color, 1); }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            g2d.setColor(lineColor);
            g2d.drawRect(x, y, width - 1, height - 1);
            g2d.setStroke(oldStroke);
        }
    }

    // Screen 3: Validation Panel
    private JPanel createValidatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CANVAS);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Data Integrity Check");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        // --- MODERN UI CHANGE: Use JTextPane for HTML ---
        validationLogArea = new JTextPane();
        validationLogArea.setEditable(false);
        validationLogArea.setContentType("text/html");
        validationLogArea.setBackground(BG_CARD);
        validationLogArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(validationLogArea);
        scroll.setBorder(new LineBorder(BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnRunValidation = new JButton("Run Validation Check");
        stylePrimaryButton(btnRunValidation);

        btnRunValidation.addActionListener(e -> {
            DataValidator validator = new DataValidator();
            List<String> errors = validator.validate(students, classrooms, masterCourses, enrolledCourses);

            StringBuilder html = new StringBuilder();
            html.append("<html><body style='font-family: SansSerif; font-size: 13px;'>");

            if (errors.isEmpty()) {
                html.append("<div style='color: #22c55e; font-weight: bold; font-size: 14px;'>✔ Validation Successful</div>");
                html.append("<div style='color: #1e293b; margin-top: 8px;'>All data integrity checks passed. You are ready to schedule.</div>");
            } else {
                html.append("<div style='color: #ef4444; font-weight: bold; font-size: 14px;'>⚠ Validation Failed</div>");
                html.append("<div style='color: #64748b; margin-top: 8px;'>Found ").append(errors.size()).append(" issues that need resolution:</div>");
                html.append("<ul style='margin-left: 20px; margin-top: 8px; color: #334155;'>");
                for (String err : errors) {
                    html.append("<li>").append(err).append("</li>");
                }
                html.append("</ul>");
            }
            html.append("</body></html>");
            validationLogArea.setText(html.toString());
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(BG_CANVAS);
        btnPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        btnPanel.add(btnRunValidation);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Screen 4: Config Panel
    // Screen 4: Config Panel (Version 1: Wide & Northwest Aligned)
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_CANVAS);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Align content to the top-left
        gbc.insets = new Insets(20, 40, 10, 40);

        // PAGE TITLE
        JLabel title = new JLabel("Exam Period Setup");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);

        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("Define the exam period structure");
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(TEXT_SECONDARY);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 30, 40);
        panel.add(subtitle, gbc);

        // CONFIG CARD (Wide Layout)
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(720, 360)); // Wider card dimensions

        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_BLUE, 2, true),
                new EmptyBorder(35, 50, 35, 50)
        ));

        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.insets = new Insets(14, 14, 14, 14);
        cgbc.fill = GridBagConstraints.HORIZONTAL;

        // Card Title
        JLabel cardTitle = new JLabel("Period Configuration");
        cardTitle.setFont(FONT_SUBHEADER);
        cardTitle.setForeground(TEXT_PRIMARY);

        cgbc.gridx = 0;
        cgbc.gridy = 0;
        cgbc.gridwidth = 2;
        card.add(cardTitle, cgbc);

        // Days Input
        JLabel lblDays = new JLabel("Total Number of Exam Days");
        lblDays.setFont(FONT_BODY);

        cgbc.gridy = 1;
        cgbc.gridwidth = 1;
        card.add(lblDays, cgbc);

        spinDays = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        spinDays.setPreferredSize(new Dimension(160, 40));
        spinDays.setFont(FONT_BODY);
        ((JSpinner.DefaultEditor) spinDays.getEditor()).getTextField().setEditable(false);

        cgbc.gridx = 1;
        card.add(spinDays, cgbc);

        // Slots Input
        JLabel lblSlots = new JLabel("Number of Slots per Day");
        lblSlots.setFont(FONT_BODY);

        cgbc.gridx = 0;
        cgbc.gridy = 2;
        card.add(lblSlots, cgbc);

        spinSlots = new JSpinner(new SpinnerNumberModel(4, -100, 100, 1));
        spinSlots.setPreferredSize(new Dimension(160, 40));
        spinSlots.setFont(FONT_BODY);
        ((JSpinner.DefaultEditor) spinSlots.getEditor()).getTextField().setEditable(false);
        
        spinSlots.addChangeListener(e -> {
            int val = (Integer) spinSlots.getValue();
            if (val > 6) {
                spinSlots.setValue(6);
                JOptionPane.showMessageDialog(this, "There can be maximum of 6 slots in a day", "Input Error", JOptionPane.ERROR_MESSAGE);
            } else if (val <=  0) {
                spinSlots.setValue(1);
                JOptionPane.showMessageDialog(this, "Slot number cannot be zero or negative", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cgbc.gridx = 1;
        card.add(spinSlots, cgbc);

        // Action Button
        JButton btnSave = new JButton("Generate Grid Preview");
        btnSave.setBackground(ACCENT_BLUE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnSave.setBorder(new EmptyBorder(14, 36, 14, 36));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSave.addActionListener(e -> {
            int days = (Integer) spinDays.getValue();
            int slots = (Integer) spinSlots.getValue();
            this.examPeriod = new ExamPeriod(days, slots);
            JOptionPane.showMessageDialog(
                    this,
                    "Exam period configured successfully!",
                    "Configuration Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        cgbc.gridx = 0;
        cgbc.gridy = 3;
        cgbc.gridwidth = 2;
        cgbc.insets = new Insets(30, 0, 0, 0);
        card.add(btnSave, cgbc);

        // ADD CARD TO PANEL
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 40, 0, 40);
        panel.add(card, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CANVAS);
        wrapper.add(panel, BorderLayout.NORTH);

        return wrapper;
    }

    // Screen 5: Scheduler Panel (UPDATED WITH LOAD FEATURE)
    private JPanel createSchedulerPanel() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_CANVAS);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // HEADER
        JLabel title = new JLabel("Scheduler");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Generate and manage the exam schedule");
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG_CANVAS);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        // CONTENT
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG_CANVAS);
        center.setBorder(new EmptyBorder(20, 0, 0, 0));

        // CONTROL CARD (Button container strip)
        JPanel controlCard = new JPanel(new BorderLayout());
        controlCard.setBackground(BG_CARD);
        controlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
        controlCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        controlCard.setPreferredSize(new Dimension(0, 60));

        // --- BUTTON GROUP ---
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonGroup.setBackground(BG_CARD);

        // 1. Generate Button
        JButton btnGenerate = new JButton("▶ Generate Schedule");
        stylePrimaryButton(btnGenerate);

        // 2. Load Saved Button (NEW FEATURE)
        JButton btnLoadSaved = new JButton("📂 Load Saved");
        btnLoadSaved.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLoadSaved.setBackground(new Color(100, 116, 139)); // Grey/Blue tone
        btnLoadSaved.setForeground(Color.WHITE);
        btnLoadSaved.setFocusPainted(false);
        btnLoadSaved.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnLoadSaved.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonGroup.add(btnGenerate);
        buttonGroup.add(btnLoadSaved);

        // Counter label on the right
        lblScheduledCount = new JLabel("Scheduled: 0 exams");
        lblScheduledCount.setFont(FONT_BODY);
        lblScheduledCount.setForeground(TEXT_SECONDARY);

        controlCard.add(buttonGroup, BorderLayout.WEST);
        controlCard.add(lblScheduledCount, BorderLayout.EAST);

        center.add(controlCard);
        center.add(Box.createVerticalStrut(20));

        // STATUS AREA
        schedulerStatusPanel = new JPanel();
        schedulerStatusPanel.setLayout(new BoxLayout(schedulerStatusPanel, BoxLayout.Y_AXIS));
        schedulerStatusPanel.setBackground(BG_CANVAS);

        JScrollPane statusScroll = new JScrollPane(schedulerStatusPanel);
        statusScroll.getVerticalScrollBar().setUnitIncrement(24);
        statusScroll.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        statusScroll.setPreferredSize(new Dimension(0, 420));

        center.add(statusScroll);

        // VIEW RESULTS BUTTON
        btnViewResults = new JButton("View Results");
        stylePrimaryButton(btnViewResults);
        btnViewResults.setVisible(false);

        // View Results Action (Fixed to update the left menu selection)
        btnViewResults.addActionListener(e -> {
            setActiveButton(btnResults); // Activate the Results button in the sidebar
            cardLayout.show(mainContentPanel, "results");
            updateResultsTable();
        });

        center.add(Box.createVerticalStrut(16));
        center.add(btnViewResults);

        root.add(center, BorderLayout.CENTER);

        // --- ACTION LISTENERS ---

        // 1. Generate Button Action
        btnGenerate.addActionListener(e -> {
            schedulerStatusPanel.removeAll();
            btnViewResults.setVisible(false);
            runSchedulingAndLog();
        });

        // 2. Load Saved Button Action (NEW FEATURE LOGIC)
        btnLoadSaved.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Saved Schedule CSV");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            // Start from the current project directory
            fileChooser.setCurrentDirectory(new File("."));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Get Config (Day/Slot) settings
                int days = (Integer) spinDays.getValue();
                int slots = (Integer) spinSlots.getValue();

                // RESET CURRENT SCHEDULE (IMPORTANT!)
                this.examPeriod = new ExamPeriod(days, slots);

                try {
                    // Invoke the importer
                    ScheduleImporter importer = new ScheduleImporter();
                    importer.importSchedule(file, this.examPeriod);

                    // If successful, update the status panel
                    schedulerStatusPanel.removeAll();
                    addStatusItem("Schedule Loaded",
                            "Successfully loaded schedule from: " + file.getName(),
                            SUCCESS_GREEN);

                    lblScheduledCount.setText("Scheduled: Loaded from File");

                    // Show the 'View Results' button
                    btnViewResults.setVisible(true);

                    // Update background tables
                    updateResultsTable();
                    updateVisualTimetable();

                    JOptionPane.showMessageDialog(this, "Schedule loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading schedule: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return root;
    }
    private void addStatusItem(String title, String message, Color color) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitle.setForeground(color);

        JLabel lblMsg = new JLabel("<html>" + message + "</html>");
        lblMsg.setFont(FONT_BODY);
        lblMsg.setForeground(TEXT_SECONDARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);

        schedulerStatusPanel.add(card);
        schedulerStatusPanel.add(Box.createVerticalStrut(10));

        schedulerStatusPanel.revalidate();
        schedulerStatusPanel.repaint();
    }
    private void runSchedulingAndLog() {

        addStatusItem("Scheduler Started",
                "Scheduling engine initialized successfully.",
                ACCENT_BLUE);

        int days = (Integer) spinDays.getValue();
        int slots = (Integer) spinSlots.getValue();
        this.examPeriod = new ExamPeriod(days, slots);

        addStatusItem("Exam Period Configuration",
                "Days: <b>" + days + "</b><br>Slots per day: <b>" + slots + "</b>",
                ACCENT_BLUE);

        // FIXED EXAMS
        FixedExamService fixedService = new FixedExamService();
        int fixedCount = 0;

        for (FixedExam fx : fixedExams) {
            try {
                fixedService.addFixedExam(fx);
                if (fx.getDay() <= days && fx.getSlot() <= slots) {
                    examPeriod.assignFixedExam(
                            fx.getDay() - 1,
                            fx.getSlot() - 1,
                            fx.getCourseCode()
                    );
                    fixedCount++;
                }
            } catch (Exception ignored) {}
        }

        addStatusItem("Fixed Exams",
                "Assigned fixed exams: <b>" + fixedCount + "</b>",
                ACCENT_BLUE);

        // RUN SCHEDULER
        ExamSchedulerService schedulerService = new ExamSchedulerService();
        List<Course> unplaced =
                schedulerService.scheduleRegularExams(enrolledCourses, classrooms, examPeriod);

        if (unplaced.isEmpty()) {

            lblScheduledCount.setText("Scheduled: " + countScheduledExams() + " exams");

            addStatusItem("Scheduling Completed",
                    "✔ All exams scheduled successfully<br>✔ No conflicts detected",
                    SUCCESS_GREEN);

            btnViewResults.setVisible(true);

        } else {

            addStatusItem("Scheduling Failed",
                    "❌ Could not place <b>" + unplaced.size() + "</b> courses",
                    ERROR_RED);

            // Capture suggestion engine output
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));

            try {
                new SuggestionEngine().analyzeAndSuggest(
                        enrolledCourses, classrooms, fixedExams, days, slots);
            } finally {
                System.setOut(original);
            }

            addStatusItem("Suggestions",
                    "<pre style='font-family:monospace'>" + baos.toString() + "</pre>",
                    new Color(234, 179, 8)); // amber
        }
    }

    private int countScheduledExams() {
        if (examPeriod == null || examPeriod.getExamMatrix() == null) return 0;

        int count = 0;
        String[][] m = examPeriod.getExamMatrix();
        for (int d = 0; d < m.length; d++) {
            for (int s = 0; s < m[d].length; s++) {
                if (m[d][s] != null) count++;
            }
        }
        return count;
    }

    // Screen 6: Results Panel (Reverted to just the list)
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CANVAS);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_CANVAS);
        toolbar.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Final Schedule");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);

        //ACTIONS
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(BG_CANVAS);

        // VIEW SELECTOR (FR-6)
        JComboBox<String> viewSelector = new JComboBox<>();
        viewSelector.addItem("View by All");
        viewSelector.addItem("View by Day");
        viewSelector.addItem("View by Course");
        viewSelector.addItem("View by Student");
        viewSelector.addItem("View by Classroom");

        // COURSE SELECTOR
        courseSelector = new JComboBox<>();
        courseSelector.addItem("Select Course");

        // DEFAULT STATES
        courseSelector.setEnabled(false);

        // VIEW CHANGE LOGIC
        viewSelector.addActionListener(e -> {

            String view = (String) viewSelector.getSelectedItem();

            courseSelector.removeAllItems();
            courseSelector.setEnabled(false);

            switch (view) {

                case "View by All":
                    updateResultsTable();
                    break;

                case "View by Day":
                    courseSelector.addItem("Select Day");
                    for (int i = 1; i <= examPeriod.getTotalDays(); i++) {
                        courseSelector.addItem("Day " + i);
                    }
                    courseSelector.setEnabled(true);
                    updateResultsTableByAllDays();
                    break;

                case "View by Course":
                    courseSelector.addItem("Select Course");
                    for (Course c : masterCourses) {
                        courseSelector.addItem(c.getCourseCode());
                    }
                    courseSelector.setEnabled(true);
                    updateResultsTableByAllCourses();
                    break;

                case "View by Student":
                    courseSelector.addItem("Select Student");
                    for (Student s : students) {
                        courseSelector.addItem(s.getId());
                    }
                    courseSelector.setEnabled(true);
                    updateResultsTableByAllStudents();
                    break;

                case "View by Classroom":
                    courseSelector.addItem("Select Classroom");
                    for (Classroom c : classrooms) {
                        courseSelector.addItem(c.getName());
                    }
                    courseSelector.setEnabled(true);
                    updateResultsTableByAllClassrooms();
                    break;
            }
        });



        // COURSE FILTER LOGIC
        courseSelector.addActionListener(e -> {
            if (!courseSelector.isEnabled()) return;

            String selected = (String) courseSelector.getSelectedItem();
            if (selected == null || selected.startsWith("Select")) return;

            String view = (String) viewSelector.getSelectedItem();

            switch (view) {
                case "View by Day":
                    updateResultsTableByDay(selected);
                    break;
                case "View by Course":
                    updateResultsTableByCourse(selected);
                    break;
                case "View by Student":
                    updateResultsTableByStudent(selected);
                    break;
                case "View by Classroom":
                    updateResultsTableByClassroom(selected);
                    break;
            }
        });



        // BUTTONS
        JButton btnExport = new JButton("Export CSV");
        stylePrimaryButton(btnExport);

        btnExport.addActionListener(e -> {

            if (examPeriod == null || examPeriod.getExamMatrix() == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No schedule available to export.",
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Schedule as CSV");

            int result = chooser.showSaveDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    String path = chooser.getSelectedFile().getAbsolutePath();

                    if (!path.endsWith(".csv")) {
                        path += ".csv";
                    }

                    ScheduleCSVExporter.export(
                            examPeriod,
                            masterCourses,
                            path
                    );

                    JOptionPane.showMessageDialog(
                            this,
                            "Schedule exported successfully!",
                            "Export Complete",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Export failed: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // SEARCH BAR
        txtSearch = new JTextField(15);
        txtSearch.setToolTipText("Filter results...");
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyCurrentSearchFilter(); }
            public void removeUpdate(DocumentEvent e) { applyCurrentSearchFilter(); }
            public void changedUpdate(DocumentEvent e) { applyCurrentSearchFilter(); }
        });

        // ADD ORDER
        actions.add(new JLabel("Search:"));
        actions.add(txtSearch);
        actions.add(viewSelector);
        actions.add(courseSelector);
        actions.add(btnExport);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // --- TABLE: DETAILED LIST ---
        String[] columnNames = {"Day", "Slot", "Course Code", "Students Enrolled"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        resultsTable = new JTable(model);

        resultsTable.setRowHeight(40);
        resultsTable.setShowVerticalLines(false);
        resultsTable.setGridColor(BORDER_COLOR);
        resultsTable.setFont(FONT_BODY);

        JTableHeader header = resultsTable.getTableHeader();
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_PRIMARY);
        header.setFont(FONT_SUBHEADER);
        header.setPreferredSize(new Dimension(0, 40));

        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void applyCurrentSearchFilter() {
        if (resultsTable.getRowSorter() instanceof TableRowSorter) {
            TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) resultsTable.getRowSorter();
            String text = txtSearch.getText();
            if (text.trim().length() == 0) {
                sorter.setRowFilter(null);
            } else {
                try {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                } catch (Exception e) {
                    // ignore invalid regex
                }
            }
        }
    }

    // Screen 7: Timetable Panel (NEW SEPARATE SCREEN)
    private JPanel createTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CANVAS);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_CANVAS);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Timetable");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Visual Timetable Table Setup
        visualTimetable = new JTable();
        visualTimetable.setRowHeight(60); // Taller rows for readability
        visualTimetable.setShowVerticalLines(true);
        visualTimetable.setGridColor(BORDER_COLOR);
        visualTimetable.setFont(FONT_BODY);
        visualTimetable.setEnabled(false); // Make it read-only/visual only

        JTableHeader visualHeader = visualTimetable.getTableHeader();
        visualHeader.setBackground(new Color(226, 232, 240)); // Slightly darker header
        visualHeader.setForeground(TEXT_PRIMARY);
        visualHeader.setFont(FONT_SUBHEADER);
        visualHeader.setPreferredSize(new Dimension(0, 40));

        JScrollPane visualScrollPane = new JScrollPane(visualTimetable);
        visualScrollPane.setBorder(new LineBorder(BORDER_COLOR));
        visualScrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(visualScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateResultsTable() {
        setTableColumns(new String[]{
                "Day", "Slot", "Course Code", "Students Enrolled"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        for (int day = 0; day < matrix.length; day++) {
            for (int slot = 0; slot < matrix[day].length; slot++) {

                String courseCode = matrix[day][slot];
                if (courseCode == null) continue;

                String finalCode = courseCode.replace("[FIXED] ", "").trim();

                Optional<Course> courseOpt = masterCourses.stream()
                        .filter(c -> c.getCourseCode().equals(finalCode))
                        .findFirst();

                int studentCount = courseOpt
                        .map(c -> c.getEnrolledStudents().size())
                        .orElse(0);

                model.addRow(new Object[]{
                        "Day " + (day + 1),
                        "Slot " + (slot + 1),
                        finalCode,
                        studentCount
                });
            }
        }
        // Always sync the visual timetable when the main data is updated
        updateVisualTimetable();
    }

    private void updateVisualTimetable() {
        if (examPeriod == null || examPeriod.getExamMatrix() == null) return;

        int days = examPeriod.getTotalDays();
        int slots = examPeriod.getSlotsPerDay();

        // 1. Create Columns: "Time Slot" + "Day 1", "Day 2"...
        String[] columns = new String[days + 1];
        columns[0] = "Time Slot";
        for (int i = 0; i < days; i++) {
            columns[i + 1] = "Day " + (i + 1);
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        String[][] matrix = examPeriod.getExamMatrix(); // [day][slot]

        // 2. Calculate Rows (Time Slots)
        // Start: 09:00. Duration: 2h (120 min) per slot
        int startHour = 9;
        int startMin = 0;
        int slotDurationMinutes = 120; // 2 hours exactly
        int examDurationMinutes = 120; // 2 hours

        for (int s = 0; s < slots; s++) {
            Object[] rowData = new Object[days + 1];

            // Calculate Start Time
            int totalStartMinutes = (startHour * 60) + startMin + (s * slotDurationMinutes);
            int sh = totalStartMinutes / 60;
            int sm = totalStartMinutes % 60;

            // Calculate End Time (Start + 2h exam duration)
            int totalEndMinutes = totalStartMinutes + examDurationMinutes;
            int eh = totalEndMinutes / 60;
            int em = totalEndMinutes % 60;

            String timeStr = String.format("%02d:%02d - %02d:%02d", sh, sm, eh, em);
            rowData[0] = timeStr;

            // Fill Data for each Day
            for (int d = 0; d < days; d++) {
                if (s < matrix[d].length) {
                    String code = matrix[d][s];
                    // Clean up fixed tag for cleaner UI
                    rowData[d + 1] = (code != null) ? code.replace("[FIXED] ", "") : "";
                } else {
                    rowData[d + 1] = "";
                }
            }
            model.addRow(rowData);
        }

        visualTimetable.setModel(model);
        // Center align columns for better look
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < visualTimetable.getColumnCount(); i++) {
            visualTimetable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        // First column (Time) bold/different color
        visualTimetable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("SansSerif", Font.BOLD, 12));
                c.setBackground(new Color(241, 245, 249));
                return c;
            }
        });
    }


    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private void updateResultsTableByDay(String dayLabel) {

        setTableColumns(new String[]{
                "Day", "Slot", "Course", "Students Enrolled"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        // "Day 3" → 3
        int selectedDay = Integer.parseInt(dayLabel.replace("Day", "").trim()) - 1;

        if (selectedDay < 0 || selectedDay >= matrix.length) return;

        for (int slot = 0; slot < matrix[selectedDay].length; slot++) {

            String courseCode = matrix[selectedDay][slot];
            if (courseCode == null) continue;

            String clean = courseCode.replace("[FIXED] ", "").trim();

            Optional<Course> courseOpt = masterCourses.stream()
                    .filter(c -> c.getCourseCode().equals(clean))
                    .findFirst();

            int studentCount = courseOpt
                    .map(c -> c.getEnrolledStudents().size())
                    .orElse(0);

            model.addRow(new Object[]{
                    "Day " + (selectedDay + 1),
                    "Slot " + (slot + 1),
                    clean,
                    studentCount
            });
        }
    }


    private void updateResultsTableByCourse(String courseCode) {

        setTableColumns(new String[]{
                "Course", "Student ID", "Day", "Slot"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        Course course = masterCourses.stream()
                .filter(c -> c.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);

        if (course == null) return;

        int examDay = -1;
        int examSlot = -1;

        outer:
        for (int day = 0; day < matrix.length; day++) {
            for (int slot = 0; slot < matrix[day].length; slot++) {
                String code = matrix[day][slot];
                if (code == null) continue;

                String clean = code.replace("[FIXED] ", "").trim();
                if (clean.equals(courseCode)) {
                    examDay = day + 1;
                    examSlot = slot + 1;
                    break outer;
                }
            }
        }

        if (examDay == -1) return;

        for (Student student : course.getEnrolledStudents()) {
            model.addRow(new Object[]{
                    courseCode,
                    student.getId(),
                    "Day " + examDay,
                    "Slot " + examSlot
            });
        }
    }

    private void refreshCourseSelector(JComboBox<String> courseSelector) {
        courseSelector.removeAllItems();
        courseSelector.addItem("Select Course");

        if (masterCourses != null) {
            for (Course c : masterCourses) {
                courseSelector.addItem(c.getCourseCode());
            }
        }
    }
    private void updateResultsTableByStudent(String studentId) {

        setTableColumns(new String[]{
                "Student ID", "Course", "Day", "Slot"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        Student selectedStudent = students.stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElse(null);

        if (selectedStudent == null) return;

        for (Course course : masterCourses) {

            if (!course.getEnrolledStudents().contains(selectedStudent)) continue;

            for (int day = 0; day < matrix.length; day++) {
                for (int slot = 0; slot < matrix[day].length; slot++) {

                    String code = matrix[day][slot];
                    if (code == null) continue;

                    String clean = code.replace("[FIXED] ", "").trim();

                    if (clean.equals(course.getCourseCode())) {
                        model.addRow(new Object[]{
                                studentId,
                                course.getCourseCode(),
                                "Day " + (day + 1),
                                "Slot " + (slot + 1)
                        });
                    }
                }
            }
        }
    }


    private void updateResultsTableByClassroom(String classroomName) {

        setTableColumns(new String[]{
                "Classroom", "Day", "Slot", "Course", "Utilization"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        Classroom selectedRoom = classrooms.stream()
                .filter(c -> c.getName().equals(classroomName))
                .findFirst()
                .orElse(null);

        if (selectedRoom == null) return;

        for (int day = 0; day < matrix.length; day++) {
            for (int slot = 0; slot < matrix[day].length; slot++) {

                String courseCode = matrix[day][slot];
                if (courseCode == null) continue;

                String clean = courseCode.replace("[FIXED] ", "").trim();

                Optional<Course> courseOpt = masterCourses.stream()
                        .filter(c -> c.getCourseCode().equals(clean))
                        .findFirst();

                int studentCount = courseOpt
                        .map(c -> c.getEnrolledStudents().size())
                        .orElse(0);

                if (studentCount <= selectedRoom.getCapacity()) {
                    model.addRow(new Object[]{
                            selectedRoom.getName(),
                            "Day " + (day + 1),
                            "Slot " + (slot + 1),
                            clean,
                            studentCount + "/" + selectedRoom.getCapacity()
                    });
                }
            }
        }
    }

    private void updateResultsTableByAllDays() {

        setTableColumns(new String[]{
                "Day", "Slot", "Course", "Students Enrolled"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        for (int day = 0; day < matrix.length; day++) {
            for (int slot = 0; slot < matrix[day].length; slot++) {

                String code = matrix[day][slot];
                if (code == null) continue;

                String clean = code.replace("[FIXED] ", "").trim();

                Optional<Course> courseOpt = masterCourses.stream()
                        .filter(c -> c.getCourseCode().equals(clean))
                        .findFirst();

                int studentCount = courseOpt
                        .map(c -> c.getEnrolledStudents().size())
                        .orElse(0);

                model.addRow(new Object[]{
                        "Day " + (day + 1),
                        "Slot " + (slot + 1),
                        clean,
                        studentCount
                });
            }
        }
    }


    private void updateResultsTableByAllCourses() {

        setTableColumns(new String[]{
                "Course", "Day", "Slot", "Students"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        for (Course course : masterCourses) {
            for (int day = 0; day < matrix.length; day++) {
                for (int slot = 0; slot < matrix[day].length; slot++) {

                    String code = matrix[day][slot];
                    if (code == null) continue;

                    if (code.replace("[FIXED] ", "").trim()
                            .equals(course.getCourseCode())) {

                        model.addRow(new Object[]{
                                course.getCourseCode(),
                                "Day " + (day + 1),
                                "Slot " + (slot + 1),
                                course.getEnrolledStudents().size()
                        });
                    }
                }
            }
        }
    }


    private void updateResultsTableByAllStudents() {

        setTableColumns(new String[]{
                "Student ID", "Course", "Day", "Slot"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        for (Student student : students) {
            for (Course course : masterCourses) {

                if (!course.getEnrolledStudents().contains(student)) continue;

                for (int day = 0; day < matrix.length; day++) {
                    for (int slot = 0; slot < matrix[day].length; slot++) {

                        String code = matrix[day][slot];
                        if (code == null) continue;

                        String clean = code.replace("[FIXED] ", "").trim();

                        if (clean.equals(course.getCourseCode())) {
                            model.addRow(new Object[]{
                                    student.getId(),
                                    course.getCourseCode(),
                                    "Day " + (day + 1),
                                    "Slot " + (slot + 1)
                            });
                        }
                    }
                }
            }
        }
    }

    private void updateResultsTableByAllClassrooms() {

        setTableColumns(new String[]{
                "Classroom", "Course", "Day", "Slot", "Students"
        });

        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) return;

        for (Classroom room : classrooms) {
            for (int day = 0; day < matrix.length; day++) {
                for (int slot = 0; slot < matrix[day].length; slot++) {

                    String code = matrix[day][slot];
                    if (code == null) continue;

                    String clean = code.replace("[FIXED] ", "").trim();

                    Optional<Course> courseOpt = masterCourses.stream()
                            .filter(c -> c.getCourseCode().equals(clean))
                            .findFirst();

                    int count = courseOpt
                            .map(c -> c.getEnrolledStudents().size())
                            .orElse(0);

                    if (count <= room.getCapacity()) {
                        model.addRow(new Object[]{
                                room.getName(),
                                clean,
                                "Day " + (day + 1),
                                "Slot " + (slot + 1),
                                count
                        });
                    }
                }
            }
        }
    }



    private void setTableColumns(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        resultsTable.setModel(model);
        
        // RE-ATTACH SORTER ON MODEL CHANGE
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        resultsTable.setRowSorter(sorter);
        
        // RE-APPLY SEARCH FILTER
        applyCurrentSearchFilter();
    }
    // gets data from database if user imported files before
    private void loadFromDatabaseIfAvailable() {

        if (!studentDAO.isEmpty()) {
            students = studentDAO.getAllStudents();
        }

        if (!courseDAO.isEmpty()) {
            masterCourses = courseDAO.getAllCourses();
        }

        if (!classroomDAO.isEmpty()) {
            classrooms = classroomDAO.getAllClassrooms();
        }

        if (!attendanceDAO.isEmpty()) {
            enrollmentService.loadEnrollments(masterCourses, students);
            enrolledCourses = masterCourses;
        }
    }
    private void setActiveButton(JButton activeBtn) {

        JButton[] allButtons = {
                btnDashboard,
                btnImport,
                btnValidate,
                btnConfig,
                btnScheduler,
                btnResults,
                btnTimetable
        };

        // Reset others
        for (JButton btn : allButtons) {
            btn.setBackground(SIDEBAR_COLOR);
            btn.setForeground(new Color(203, 213, 225));
            btn.setBorder(MENU_NORMAL_BORDER);
        }

        activeBtn.setBackground(new Color(55, 72, 95));
        activeBtn.setForeground(Color.WHITE);

        // Stop previous animation if exists
        if (activeAnimTimer != null && activeAnimTimer.isRunning()) {
            activeAnimTimer.stop();
        }

        currentInset = 20;

        activeAnimTimer = new Timer(15, e -> {
            currentInset += 2;

            activeBtn.setBorder(createAnimatedActiveBorder(currentInset));
            activeBtn.revalidate();
            activeBtn.repaint();

            if (currentInset >= TARGET_INSET) {
                ((Timer) e.getSource()).stop();
            }
        });
        activeAnimTimer.start();
    }

    private Border createAnimatedActiveBorder(int leftInset) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT_BLUE),
                BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(255, 255, 255, 25), 1),
                        new EmptyBorder(10, leftInset, 10, 15)
                )
        );
    }
}