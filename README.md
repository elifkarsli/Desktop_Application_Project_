🎓 University Exam Scheduler

University Exam Scheduler is a desktop application developed in Java for schools and universities. It automatically creates exam schedules, making the process faster and more reliable than manual planning, which is often difficult and time-consuming.

🎯 Purpose of the Program

The main goal of this program is to simplify exam scheduling. It prevents students from having two exams at the same time, ensures that classrooms have enough capacity, respects exams with fixed dates and times, and reduces human errors while saving time.

⚙️ What the Program Does

The program reads student, course, classroom, and attendance data from CSV files. It checks whether the data is valid, detects conflicts between exams, and automatically generates a suitable exam schedule. The final schedule is displayed on the screen and can also be saved as a file.

📂 Input Files (CSV)

All required CSV files must be placed in a folder named CSV_Files.
The program uses the following files:

students.csv – contains student IDs

courses.csv – contains course codes

classrooms.csv – contains classroom names and capacities

attendance.csv – shows which students are enrolled in which courses

fixed_exams.csv (optional) – contains exams with fixed times

These CSV files can be created using Excel or Google Sheets.

🧠 How It Works (Simple Explanation)

First, the program checks all input data. Then it identifies exam conflicts, making sure that exams with common students are not scheduled at the same time. Exams with fixed times remain unchanged. The system tries different scheduling options until it finds a valid solution.

🖥️ How to Use the Program

Install Java 8 or a newer version on your computer. Place all CSV files into the CSV_Files folder and run the program using the command:

java -jar UniversitySchedulerApp.jar

You can then use the buttons in the interface to import data, create the exam schedule, and export the results.

📤 Output

The generated schedule includes exam times and classroom names, displayed in a clear table format. The schedule can be saved as a CSV or text file.

⚠️ Common Problems

In some cases, the program may not find a valid schedule. This can happen if there are not enough time slots, too many exam conflicts, classrooms with insufficient capacity, or incorrect or missing CSV files. If the program does not start, make sure Java is properly installed.

📁 Project Structure

UniversitySchedulerApp.java – main application file

gui/ – user interface components

model/ – student, course, and classroom classes

service/ – scheduling logic

parser/ – CSV file readers

✅ Final Notes

This program is free to use for educational purposes. It is easy to understand, modify, and extend, and it helps educational institutions plan exams efficiently and without stress.
