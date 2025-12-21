package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.ExamPeriod;
import Desktop_Application_Project_.exception.DataImportException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ScheduleImporter {

    public void importSchedule(File file, ExamPeriod examPeriod) throws DataImportException {
        // Expected CSV Format: Day 1, Slot 1, CourseCode_MATH101, ...

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                // Skip the header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                // We need at least 3 data points: Day, Slot, CourseCode
                if (parts.length < 3) continue;

                try {
                    // Parse "Day 1" to get just "1" and convert to 0-based index
                    String dayPart = parts[0].toLowerCase().replace("day", "").trim();
                    int dayIndex = Integer.parseInt(dayPart) - 1;

                    // Parse "Slot 1" to get just "1" and convert to 0-based index
                    String slotPart = parts[1].toLowerCase().replace("slot", "").trim();
                    int slotIndex = Integer.parseInt(slotPart) - 1;

                    // Get the Course Code
                    String courseCode = parts[2].trim();

                    // Assign to the matrix
                    examPeriod.assignFixedExam(dayIndex, slotIndex, courseCode);

                } catch (Exception e) {
                    System.err.println("Skipping line due to error: " + line);
                }
            }
        } catch (IOException e) {
            throw new DataImportException("Failed to read schedule file: " + file.getName(), e);
        }
    }
}