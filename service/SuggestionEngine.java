package Desktop_Application_Project_.service;

import Desktop_Application_Project_.model.DomainModels.Classroom;
import Desktop_Application_Project_.model.DomainModels.Course;
import Desktop_Application_Project_.model.DomainModels.FixedExam;
import Desktop_Application_Project_.model.DomainModels.ExamPeriod;


import java.util.ArrayList;
import java.util.List;

// This class helps us find a solution if the scheduler fails.
// It tries to see if adding more days or slots would fix the problem.
public class SuggestionEngine {

    public void analyzeAndSuggest(
            List<Course> allCourses,
            List<Classroom> classrooms,
            List<FixedExam> fixedExams,
            int currentDays,
            int currentSlots
    ) {
        System.out.println("\n=================================================");
        System.out.println("   [!] SCHEDULE INCOMPLETE - LOOKING FOR SOLUTIONS");
        System.out.println("=================================================");
        System.out.println("Analysis: Current setup (" + currentDays + " Days, " + currentSlots + " Slots) is not enough.");
        System.out.println("Thinking of alternatives...");

        boolean foundSolution = false;

        // A 1: Try adding more days (up to 5 extra days)
        for (int i = 1; i <= 5; i++) {
            int targetDays = currentDays + i;
            
            // Check if this new number of days works
            if (simulate(allCourses, classrooms, fixedExams, targetDays, currentSlots)) {
                System.out.println("\n>>> SUGGESTION A: Add " + i + " Day(s)");
                System.out.println("    New Config: " + targetDays + " Days, " + currentSlots + " Slots.");
                System.out.println("    Result: Everything fits perfectly!");
                foundSolution = true;
                break; // We found the smallest number of days needed
            }
        }

        // A 2: If adding days didn't work, try adding more slots per day (up to 3 extra slots)
        if (!foundSolution) {
            for (int i = 1; i <= 3; i++) {
                int targetSlots = currentSlots + i;
                if (simulate(allCourses, classrooms, fixedExams, currentDays, targetSlots)) {
                    System.out.println("\n>>> SUGGESTION B: Add " + i + " Slot(s) per Day");
                    System.out.println("    New Config: " + currentDays + " Days, " + targetSlots + " Slots.");
                    System.out.println("    Result: Everything fits perfectly!");
                    foundSolution = true;
                    break;
                }
            }
        }

        // If nothing worked...
        if (!foundSolution) {
            System.out.println("\n>>> SUGGESTION C: We have a big problem.");
            System.out.println("    Maybe try increasing BOTH days and slots, or check if students have too many overlapping exams.");
        }
        System.out.println("=================================================\n");
    }

    // This helper method runs a "simulation" to see if a specific day/slot combo works.
    private boolean simulate(
            List<Course> courses,
            List<Classroom> classrooms,
            List<FixedExam> fixedExams,
            int d, int s
    ) {
        // 1. Create a temporary exam period for testing
        ExamPeriod testPeriod = new ExamPeriod(d, s);
        
        // We need a fresh scheduler for this test
        ExamSchedulerService scheduler = new ExamSchedulerService();
        
        // 2. Put the fixed exams into the calendar first
        for (FixedExam fx : fixedExams) {
            // Only add them if they fit in our new day/slot limits
            if (fx.getDay() <= d && fx.getSlot() <= s) {
                // Placing the fixed exam (subtract 1 because arrays start at 0)
                testPeriod.assignFixedExam(fx.getDay() - 1, fx.getSlot() - 1, fx.getCourseCode());
            }
        }

        // 3. Try to schedule the normal exams
        List<Course> coursesCopy = new ArrayList<>(courses);

        List<Course> unplaced = scheduler.scheduleRegularExams(coursesCopy, classrooms, testPeriod);
        
        // 4. If the 'unplaced' list is empty, it means everything fit!
        return unplaced.isEmpty();
    }
}