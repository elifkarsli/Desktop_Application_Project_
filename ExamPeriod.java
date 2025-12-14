public class ExamPeriod {

    private int totalDays;        // Kaç gün sınav olacak
    private int slotsPerDay;      // Gün başına kaç sınav slotu
    private String[][] examMatrix; // Gün slot matrisi

    public ExamPeriod(int totalDays, int slotsPerDay) {
        this.totalDays = totalDays;
        this.slotsPerDay = slotsPerDay;
        this.examMatrix = new String[totalDays][slotsPerDay];
    }

    public int getTotalDays() {
        return totalDays;
    }

    public int getSlotsPerDay() {
        return slotsPerDay;
    }

    public String[][] getExamMatrix() {
        return examMatrix;
    }

    public void assignExam(int day, int slot, String courseName) {
        this.examMatrix[day][slot] = courseName;
    }

    public void printExamSchedule() {
        System.out.println("Exam Schedule:");
        for (int d = 0; d < totalDays; d++) {
            System.out.print("Day " + (d + 1) + ": ");
            for (int s = 0; s < slotsPerDay; s++) {
                System.out.print((examMatrix[d][s] != null ? examMatrix[d][s] : "empty") + " | ");
            }
            System.out.println();
        }
    }
    public boolean assignFixedExam(int day, int slot, String courseCode) {
        if (examMatrix[day][slot] != null) {
            return false; // slot dolu
        }
        examMatrix[day][slot] = "[FIXED] " + courseCode;
        return true;
    }

}