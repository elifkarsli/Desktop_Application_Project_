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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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
    private JComboBox<String> courseSelector;
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final ClassroomDAO classroomDAO = new ClassroomDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final CourseEnrollmentService enrollmentService = new CourseEnrollmentService();

    // Scheduling Objects
    private ExamPeriod examPeriod;
    private List<FixedExam> fixedExams = new ArrayList<>();

    // Navigation Buttons
    private JButton btnDashboard, btnImport, btnValidate, btnConfig, btnScheduler, btnResults;

    // Main Content Area (CardLayout for switching screens)
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private boolean isUpdatingFilter = false;

    // UI Components for updates
    private JLabel lblStudentCount, lblClassroomCount, lblCourseCount, lblAttendanceCount;
    private JTable resultsTable;
    private JSpinner spinDays, spinSlots;
    private JTextArea validationLogArea;

    // --- DESIGN SYSTEM CONSTANTS ---
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

    public SchedulerGUI(List<Student> students, List<Classroom> classrooms, List<Course> masterCourses, List<Course> enrolledCourses) {
        this.students = students;
        this.classrooms = classrooms;
        this.masterCourses = masterCourses;
        this.enrolledCourses = enrolledCourses;
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

        // --- Add Screens (Cards) ---
        mainContentPanel.add(createDashboardPanel(), "dashboard");
        mainContentPanel.add(createImportPanel(), "import");
        mainContentPanel.add(createValidatePanel(), "validate");
        mainContentPanel.add(createConfigPanel(), "config");
        mainContentPanel.add(createSchedulerPanel(), "scheduler");
        mainContentPanel.add(createResultsPanel(), "results");

        add(mainContentPanel, BorderLayout.CENTER);

        // Initial Stat Update
        updateStats();

        refreshCourseSelector(courseSelector);
        updateResultsTable();
        updateStats();

    }

    // --- Helper Methods to Build UI ---

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

        btnDashboard.addActionListener(e -> cardLayout.show(mainContentPanel, "dashboard"));
        btnImport.addActionListener(e -> cardLayout.show(mainContentPanel, "import"));
        btnValidate.addActionListener(e -> cardLayout.show(mainContentPanel, "validate"));
        btnConfig.addActionListener(e -> cardLayout.show(mainContentPanel, "config"));
        btnScheduler.addActionListener(e -> cardLayout.show(mainContentPanel, "scheduler"));
        btnResults.addActionListener(e -> cardLayout.show(mainContentPanel, "results"));

        sidebar.add(btnDashboard);
        sidebar.add(btnImport);
        sidebar.add(btnValidate);
        sidebar.add(btnConfig);
        sidebar.add(btnScheduler);
        sidebar.add(btnResults);

        // Spacer to push content up
        sidebar.add(Box.createGlue());

        return sidebar;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(SIDEBAR_COLOR); // Transparent-ish look
        btn.setForeground(new Color(203, 213, 225)); // Lighter text
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
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

        JLabel lblHeader = new JLabel("   Exam Management Console");
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblHeader.setForeground(TEXT_PRIMARY);

        JLabel lblToast = new JLabel("System Ready   ");
        lblToast.setForeground(SUCCESS_GREEN);
        lblToast.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblToast.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));

        topBar.add(lblHeader, BorderLayout.WEST);
        topBar.add(lblToast, BorderLayout.EAST);
        return topBar;
    }

    // --- Screen 1: Dashboard Panel ---
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
        JLabel lblSub = new JLabel("Welcome back, Administrator. Here is what's happening today.");
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

        statsPanel.add(createMetricCard("Students", lblStudentCount, "+12% vs last term"));
        statsPanel.add(createMetricCard("Classrooms", lblClassroomCount, "No changes"));
        statsPanel.add(createMetricCard("Courses", lblCourseCount, "+5 New Added"));
        statsPanel.add(createMetricCard("Records", lblAttendanceCount, "Updated Just Now"));

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
        activityList.setText("• System initialized successfully.\n• Waiting for data import...\n• No conflicts detected in previous run.");
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

    // --- Screen 2: Import Panel (Drag & Drop Zone Design) ---
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
                // Parsing Logic
                Parser<Student> studentParser = new CoreParsers.StudentParser();
                students = studentParser.parse(new File(txtStudent.getText()));

                Parser<Course> courseParser = new CoreParsers.CourseParser();
                masterCourses = courseParser.parse(new File(txtCourse.getText()));

                Parser<Classroom> roomParser = new CoreParsers.ClassroomParser();
                classrooms = roomParser.parse(new File(txtRoom.getText()));

                //  HANDLE RAW TYPE FOR ATTENDANCE PARSER ---
                Parser attendanceParser = new CoreParsers.AttendanceParser();
                List<?> rawAttendanceData = attendanceParser.parse(new File(txtAtt.getText()));

                // HANDLE ATTENDANCE IMPORT (CRITICAL FIX) ---
                if (!rawAttendanceData.isEmpty() && rawAttendanceData.get(0) instanceof String[]) {

                    List<String[]> rows = (List<String[]>) rawAttendanceData;

                    //Clear previous enrollments
                    for (Course c : masterCourses) {
                        c.getEnrolledStudents().clear();
                    }

                    //Manually link students to courses
                    for (String[] row : rows) {
                        if (row.length < 2) continue;

                        String studentId = row[0];
                        String courseCode = row[1];

                        Optional<Course> courseOpt = masterCourses.stream()
                                .filter(c -> c.getCourseCode().equals(courseCode))
                                .findFirst();

                        Optional<Student> studentOpt = students.stream()
                                .filter(s -> s.getId().equals(studentId))
                                .findFirst();

                        if (courseOpt.isPresent() && studentOpt.isPresent()) {
                            courseOpt.get().enrollStudent(studentOpt.get());
                        }
                    }

                    enrolledCourses = masterCourses;

                    StudentDAO studentDAO = new StudentDAO();
                    CourseDAO courseDAO = new CourseDAO();
                    ClassroomDAO classroomDAO = new ClassroomDAO();
                    AttendanceDAO attendanceDAO = new AttendanceDAO();
                    // Insert base entities
                    studentDAO.insertStudents(students);
                    courseDAO.insertCourses(masterCourses);
                    classroomDAO.insertClassrooms(classrooms);
                    // Insert attendance relations
                    List<String[]> attendanceRowsForDB = new ArrayList<>();

                    for (Course c : masterCourses) {
                        for (Student s : c.getEnrolledStudents()) {
                            attendanceRowsForDB.add(
                                    new String[]{s.getId(), c.getCourseCode()}
                            );
                        }
                    }

                    attendanceDAO.insertAttendance(attendanceRowsForDB);


                } else if (!rawAttendanceData.isEmpty() && rawAttendanceData.get(0) instanceof Course) {
                    // Legacy Format Detected (List<Course>)
                    enrolledCourses = (List<Course>) rawAttendanceData;
                } else {
                    // Empty list or unknown type
                    enrolledCourses = new ArrayList<>();
                }

                if (new File(txtFixed.getText()).exists()) {
                    Parser<FixedExam> fixedParser = new CoreParsers.FixedExamParser();
                    fixedExams = fixedParser.parse(new File(txtFixed.getText()));
                }

                // Refresh course selector AFTER import
                courseSelector.removeAllItems();
                courseSelector.addItem("Select Course");

                for (Course c : masterCourses) {
                    courseSelector.addItem(c.getCourseCode());
                }

                updateStats();
                JOptionPane.showMessageDialog(this, "Data Loaded Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (DataImportException ex) {
                JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
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

    // --- Screen 3: Validation Panel ---
    private JPanel createValidatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CANVAS);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Data Integrity Check");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        validationLogArea = new JTextArea();
        validationLogArea.setEditable(false);
        validationLogArea.setFont(FONT_MONO);
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

            validationLogArea.setText("");
            if (errors.isEmpty()) {
                validationLogArea.append("✔ SUCCESS: All data is valid.\n");
                validationLogArea.append("✔ Referential integrity checks passed.\n");
                validationLogArea.setForeground(SUCCESS_GREEN);
            } else {
                validationLogArea.append("⚠ WARNING: Found " + errors.size() + " issues:\n");
                for (String err : errors) {
                    validationLogArea.append("✖ " + err + "\n");
                }
                validationLogArea.setForeground(ERROR_RED);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(BG_CANVAS);
        btnPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        btnPanel.add(btnRunValidation);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Screen 4: Config Panel ---
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_CANVAS);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(20, 40, 10, 40); // ← sola yaklaştırdık

        // ===== PAGE TITLE =====
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

        // ===== CONFIG CARD =====
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(720, 360)); // ← DAHA BÜYÜK

        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(59, 130, 246), 2, true),
                new EmptyBorder(35, 50, 35, 50)
        ));

        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.insets = new Insets(14, 14, 14, 14);
        cgbc.fill = GridBagConstraints.HORIZONTAL;

        // ---- Card Title ----
        JLabel cardTitle = new JLabel("Period Configuration");
        cardTitle.setFont(FONT_SUBHEADER);
        cardTitle.setForeground(TEXT_PRIMARY);

        cgbc.gridx = 0;
        cgbc.gridy = 0;
        cgbc.gridwidth = 2;
        card.add(cardTitle, cgbc);

        // ---- Days ----
        JLabel lblDays = new JLabel("Total Number of Exam Days");
        lblDays.setFont(FONT_BODY);

        cgbc.gridy = 1;
        cgbc.gridwidth = 1;
        card.add(lblDays, cgbc);

        spinDays = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        spinDays.setPreferredSize(new Dimension(160, 40));
        spinDays.setFont(FONT_BODY);

        cgbc.gridx = 1;
        card.add(spinDays, cgbc);

        // ---- Slots ----
        JLabel lblSlots = new JLabel("Number of Slots per Day");
        lblSlots.setFont(FONT_BODY);

        cgbc.gridx = 0;
        cgbc.gridy = 2;
        card.add(lblSlots, cgbc);

        spinSlots = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        spinSlots.setPreferredSize(new Dimension(160, 40));
        spinSlots.setFont(FONT_BODY);

        cgbc.gridx = 1;
        card.add(spinSlots, cgbc);

        // ---- Button ----
        JButton btnSave = new JButton("Generate Grid Preview");
        btnSave.setBackground(new Color(59, 130, 246));
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

        // ===== ADD CARD =====
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 40, 0, 40); // ← KART YUKARI ALINDI
        panel.add(card, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CANVAS);
        wrapper.add(panel, BorderLayout.NORTH);

        return wrapper;
    }


    // --- Screen 5: Scheduler Panel (Terminal Design) ---
    private JPanel createSchedulerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_CANVAS);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel title = new JLabel("Scheduler Execution");
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(title, gbc);

        // Terminal-like Log Area
        JTextArea logArea = new JTextArea(20, 60);
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 23, 42)); // Very Dark Slate
        logArea.setForeground(new Color(74, 222, 128)); // Terminal Green
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setText("> Waiting for command...\n");
        logArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(new LineBorder(BORDER_COLOR));
        gbc.gridy = 1;
        panel.add(scroll, gbc);

        JButton btnRun = new JButton("▶ Start Automatic Scheduling");
        stylePrimaryButton(btnRun);
        btnRun.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnRun.setPreferredSize(new Dimension(300, 50));

        btnRun.addActionListener(e -> {
            logArea.setText("> Initializing Scheduler...\n");

            int days = (Integer) spinDays.getValue();
            int slots = (Integer) spinSlots.getValue();
            this.examPeriod = new ExamPeriod(days, slots);
            logArea.append("> Exam Period Set: " + days + " days, " + slots + " slots.\n");

            FixedExamService fixedService = new FixedExamService();
            int fixedCount = 0;
            for (FixedExam fx : fixedExams) {
                try {
                    fixedService.addFixedExam(fx);
                    if (fx.getDay() <= days && fx.getSlot() <= slots) {
                        examPeriod.assignFixedExam(fx.getDay() - 1, fx.getSlot() - 1, fx.getCourseCode());
                        fixedCount++;
                    } else {
                         logArea.append("! WARNING: Fixed exam " + fx.getCourseCode() + " out of bounds!\n");
                    }
                } catch (Exception ex) {
                     logArea.append("! Conflict: " + ex.getMessage() + "\n");
                }
            }
            logArea.append("> Assigned " + fixedCount + " fixed exams.\n");

            ExamSchedulerService schedulerService = new ExamSchedulerService();
            List<Course> unplaced = schedulerService.scheduleRegularExams(enrolledCourses, classrooms, examPeriod);

            if (unplaced.isEmpty()) {
                logArea.append("> SUCCESS: All exams scheduled successfully!\n");
                logArea.append("> Process finished with code 0.");
                JOptionPane.showMessageDialog(this, "Scheduling Complete!");
                cardLayout.show(mainContentPanel, "results");
                updateResultsTable();
            } else {
                logArea.append("> FAILURE: Could not place " + unplaced.size() + " courses.\n");
                logArea.append("> Triggering Suggestion Engine...\n");

                // --- CAPTURE SUGGESTION ENGINE OUTPUT ---
                PrintStream originalOut = System.out;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream captureOut = new PrintStream(baos);

                try {
                    System.setOut(captureOut);
                    SuggestionEngine suggestionEngine = new SuggestionEngine();
                    suggestionEngine.analyzeAndSuggest(enrolledCourses, classrooms, fixedExams, days, slots);
                    System.out.flush();
                } finally {
                    System.setOut(originalOut);
                }

                logArea.append(baos.toString());

                logArea.append("> Check above for detailed suggestions.\n");
                JOptionPane.showMessageDialog(this, "Scheduling Failed. Check terminal for details.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        gbc.gridy = 2;
        panel.add(btnRun, gbc);

        return panel;
    }
    private void refreshTableAccordingToCurrentState(String view) {

        String selected = (String) courseSelector.getSelectedItem();

        if (selected == null || selected.startsWith("Select")) {
            switch (view) {
                case "View by Day":
                    updateResultsTableByAllDays();
                    return;
                case "View by Course":
                    updateResultsTableByAllCourses();
                    return;
                case "View by Student":
                    updateResultsTableByAllStudents();
                    return;
                case "View by Classroom":
                    updateResultsTableByAllClassrooms();
                    return;
                default:
                    updateResultsTable();
                    return;
            }
        }

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
    }


    // --- Screen 6: Results Panel (Modern Data Table) ---
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

        // ---------- ACTIONS ----------
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


        // ADD ORDER
        actions.add(viewSelector);
        actions.add(courseSelector);
        actions.add(btnExport);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // ---------- TABLE ----------
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
    }


    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
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

    private int getStudentCount(String courseCode) {
        return masterCourses.stream()
                .filter(c -> c.getCourseCode().equals(courseCode))
                .map(c -> c.getEnrolledStudents().size())
                .findFirst()
                .orElse(0);
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
    }
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
}
