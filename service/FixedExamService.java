package Desktop_Application_Project_.service;
import Desktop_Application_Project_.model.DomainModels.FixedExam;

import java.util.ArrayList;
import java.util.List;

public class FixedExamService {

    private final List<FixedExam> fixedExams = new ArrayList<>();
    public List<FixedExam> getFixedExams() {
        return fixedExams;
    }

    public void addFixedExam(FixedExam fixedExam) {
        if (isConflict(fixedExam)) {
            throw new IllegalArgumentException("Fixed exam conflict detected!");
        }
        fixedExams.add(fixedExam);  
    }

    public boolean isConflict(FixedExam exam) {
        for (FixedExam fx : fixedExams) {
            boolean sameDay = fx.getDay() == exam.getDay();
            boolean sameSlot = fx.getSlot() == exam.getSlot();
            boolean sameClassroom =
                    fx.getClassroom().equals(exam.getClassroom());

            if (sameDay && sameSlot && sameClassroom) {
                return true;
            }
        }
        return false;
    }
}