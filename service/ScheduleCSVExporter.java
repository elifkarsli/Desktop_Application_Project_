package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.ExamPeriod;

import java.io.FileWriter;
import java.util.List;
import java.util.Optional;

public class ScheduleCSVExporter {

    public static void export(
            ExamPeriod examPeriod,
            List<Course> masterCourses,
            String filePath
    ) throws Exception {

        FileWriter writer = new FileWriter(filePath);
        writer.append("Day,Slot,Course Code,Students Enrolled\n");

        String[][] matrix = examPeriod.getExamMatrix();
        if (matrix == null) {
            writer.close();
            return;
        }

        for (int day = 0; day < matrix.length; day++) {
            for (int slot = 0; slot < matrix[day].length; slot++) {

                String courseCode = matrix[day][slot];
                if (courseCode == null) continue;

                String cleanCode = courseCode.replace("[FIXED] ", "").trim();

                Optional<Course> courseOpt = masterCourses.stream()
                        .filter(c -> c.getCourseCode().equals(cleanCode))
                        .findFirst();

                int studentCount = courseOpt
                        .map(c -> c.getEnrolledStudents().size())
                        .orElse(0);

                writer.append(
                        "Day " + (day + 1) + "," +
                                "Slot " + (slot + 1) + "," +
                                cleanCode + "," +
                                studentCount + "\n"
                );
            }
        }

        writer.flush();
        writer.close();
    }
}
