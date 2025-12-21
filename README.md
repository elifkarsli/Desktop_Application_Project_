🎓 University Exam Scheduler 

University Exam Scheduler is a desktop Java program. 
It helps schools and universities create exam schedules automatically. 

Making exam schedules by hand is hard and takes a lot of time. 
This program does it faster and without mistakes. 

 

🎯 What Is This Program For? 

The program helps to: 
Stop students from having two exams at the same time 
Make sure classrooms are big enough 
Respect exams with fixed times 
Save time and avoid human errors 

 

⚙️ What Does the Program Do? 

Reads student, course, and classroom data from CSV files 
Checks if the data is correct 
Finds exam conflicts between courses 
Creates an exam schedule automatically 
Shows the schedule on the screen 
Saves the schedule as a file 

 📂 Input Files (CSV) 

The program uses CSV files inside a folder called CSV_Files. 
You need these files: 
students.csv → list of student IDs 
courses.csv → list of course codes 
classrooms.csv → classroom names and capacities 
attendance.csv → which student takes which course 
fixed_exams.csv (optional) → exams with fixed time 
You can create CSV files with Excel or Google Sheets. 

 

🧠 How It Works (Simple) 

The program checks all data 
It looks for exam conflicts 
Exams with common students are not placed at the same time 
Fixed exams stay at their given time 
The program tries different options It stops when it finds a correct schedule 

 

🖥️ How to Use 

Install Java 8 or newer 
Put CSV files into the CSV_Files folder 
Run the program:  java -jar UniversitySchedulerApp.jar 
 Use the buttons: 
Schedule Exams → create schedule 
Import Data → load new CSV files 
Export → save the schedule 

 

📤 Output 

Exam time 
Classroom name 
Easy table view 
Can be saved as CSV or text file 

 

⚠️ Common Problems 

No schedule found 
Not enough time slots 
Too many conflicts 
Classroom too small 
More students than classroom capacity 
File not found 
CSV files are in the wrong folder 
Program does not start 
Check Java 

 

📁 Project Structure 

UniversitySchedulerApp.java → main file 
gui/ → user interface 
model/ → students, courses, classrooms 
service/ → scheduling logic 
parser/ → CSV file reader 

 

✅ Final Notes 

Free to use for education 
Easy to understand and change 
Helps schools plan exams without stress
