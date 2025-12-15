package Desktop_Application_Project_.service;

import Desktop_Application_Project_.ExamPeriod;
import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.FixedExam;

import java.util.List;


// Service responsible for suggesting changes to the exam period configuration  when the scheduler fails to place all courses.

 
public class SuggestionEngine {

    
    public void analyzeAndSuggest(
            List<Course> allCourses,
            List<Classroom> classrooms,
            List<FixedExam> fixedExams,
            int currentDays,
            int currentSlots
    ) {
        System.out.println("\n=================================================");
        System.out.println("   [!] SCHEDULE INCOMPLETE - SUGGESTION");
        System.out.println("=================================================");
        System.out.println("Analysis: Current config (" + currentDays + " Days, " + currentSlots + " Slots) is insufficient.");
        System.out.println("Calculating alternatives...");

        ExamSchedulerService testScheduler = new ExamSchedulerService();
        boolean foundSolution = false;

        // Strategy 1: Suggest extending the Exam Period (Add Days) ** extra 5 days
        for (int i = 1; i <= 5; i++) {
            int targetDays = currentDays + i;
            if (simulate(testScheduler, allCourses, classrooms, fixedExams, targetDays, currentSlots)) {
                System.out.println("\n>>> SUGGESTION A: Add " + i + " Day(s)");
                System.out.println("    New Configuration: " + targetDays + " Days, " + currentSlots + " Slots.");
                System.out.println("    Result: All exams can be placed successfully.");
                foundSolution = true;
                break; // Stop after finding the smallest increase
            }
        }

        // Strategy 2: Suggest extending Daily Capacity (Add Slots) ** extra 3 slots
        for (int i = 1; i <= 3; i++) {
            int targetSlots = currentSlots + i;
            if (simulate(testScheduler, allCourses, classrooms, fixedExams, currentDays, targetSlots)) {
                System.out.println("\n>>> SUGGESTION B: Add " + i + " Slot(s) per Day");
                System.out.println("    New Configuration: " + currentDays + " Days, " + targetSlots + " Slots.");
                System.out.println("    Result: All exams can be placed successfully.");
                foundSolution = true;
                break;
            }
        }

        if (!foundSolution) {
            System.out.println("\n>>> SUGGESTION C: Significant constraint issue detected.");
            System.out.println("    Consider both increasing days AND slots, or checking for massive student conflicts.");
        }
        System.out.println("=================================================\n");
    }

     // Runs a simulation with specific Day/Slot parameters to see if it works.
    private boolean simulate(
            ExamSchedulerService scheduler,
            List<Course> courses,
            List<Classroom> classrooms,
            List<FixedExam> fixedExams,
            int d, int s
    ) {
        // Create temp ExamPeriod
        ExamPeriod testPeriod = new ExamPeriod(d, s);
        
        //  Pre-assign Fixed Exams (handling 1-based index from CSV)
        for (FixedExam fx : fixedExams) {
            // Only assign if it fits in the new dimensions
            if (fx.getDay() <= d && fx.getSlot() <= s) {
                testPeriod.assignFixedExam(fx.getDay() - 1, fx.getSlot() - 1, fx.getCourseCode());
            }
        }

        // 3. Try Scheduling Regular Exams
        List<Course> unplaced = scheduler.scheduleRegularExams(courses, classrooms, testPeriod);
        
        // 4. If list is empty, simulation was successful
        return unplaced.isEmpty();
    }
}