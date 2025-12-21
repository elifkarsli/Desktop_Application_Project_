⚠️ IMPORTANT: When installing, do not save the program in Program Files (x86). Choose another folder to avoid permission issues. (MANDATORY)

📂 Desktop Application Project Features

The features folder contains detailed explanations of the program’s functionalities. You can check each feature to understand how the exam scheduler works and what operations are supported.

🎓 University Exam Scheduler

University Exam Scheduler is a desktop application developed in Java for schools and universities. It automatically creates exam schedules, making the process faster and more reliable than manual planning, which is often difficult and time-consuming.

🎯 Purpose of the Program

The main goal of this program is to simplify exam scheduling. It prevents students from having two exams at the same time, ensures that classrooms have enough capacity, respects exams with fixed dates and times, and reduces human errors while saving time.

⚙️ What the Program Does

The program reads student, course, classroom, and attendance data from CSV files. It checks whether the data is valid, detects conflicts between exams, and automatically generates a suitable exam schedule. The final schedule is displayed on the screen and can also be saved as a file.

🧠 How It Works (Simple Explanation)

First, the program checks all input data. Then it identifies exam conflicts, making sure that exams with common students are not scheduled at the same time. Exams with fixed times remain unchanged. The system tries different scheduling options until it finds a valid solution.

🖥️ How to Use the Program

Install minimum Java 17 or a newer version on your computer.

java -jar UniversitySchedulerApp.jar

You can then use the buttons in the interface to import data, create the exam schedule, and export the results.

📤 Output

The generated schedule includes exam times and classroom names, displayed in a clear table format. The schedule can be saved as a CSV.

⚠️ Common Problems

In some cases, the program may not find a valid schedule. This can happen if there are not enough time slots, too many exam conflicts, classrooms with insufficient capacity, or incorrect or missing CSV files. If the program does not start, make sure Java is properly installed.

📁 Project Structure

UniversitySchedulerApp.java – main application file

gui/ – user interface components

model/ – student, course, and classroom classes

service/ – scheduling logic

parser/ – CSV file readers

exception/ - custom exception class

✅ Final Notes

This program is free to use for educational purposes. It is easy to understand, modify, and extend, and it helps educational institutions plan exams efficiently and without stress.
