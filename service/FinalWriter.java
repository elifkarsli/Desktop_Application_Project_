package Desktop_Application_Project_.service;
import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.model.DomainModels.Student;
import Desktop_Application_Project_.model.DomainModels.Course;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

public class FinalWriter {

    public void writeOutput(
            List<Classroom> classrooms,
            List<Student> students,
            List<Course> courses,
            List<Course> attendanceList,
            String filePath) {

        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // create output folder if missing
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));


            bw.write("=== CLASSROOMS ===");
            bw.newLine();
            for (Classroom c : classrooms) {
                bw.write(c.getName() + " | Capacity: " + c.getCapacity());
                bw.newLine();
            }

            bw.newLine();
            bw.write("=== STUDENTS ===");
            bw.newLine();
            for (Student s : students) {
                bw.write(s.getId());
                bw.newLine();
            }

            bw.newLine();
            bw.write("=== COURSES ===");
            bw.newLine();
            for (Course c : courses) {

                String studentStr = c.getEnrolledStudents().stream()
                        .map(Student::getId)
                        .collect(Collectors.joining(", "));

                bw.write(c.getCourseCode() + " -> " + studentStr);
                bw.newLine();
            }

            bw.newLine();
            bw.write("=== ATTENDANCE LIST ===");
            bw.newLine();
            for (Course a : attendanceList) {
                String studentIds = a.getEnrolledStudents().stream()
                        .map(Student::getId)
                        .collect(Collectors.joining(", "));

                bw.write(a.getCourseCode() + " -> " + studentIds);
                bw.newLine();
            }

            bw.close();
            System.out.println("Output written successfully to: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}