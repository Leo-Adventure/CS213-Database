import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CSV_ex {
    public static void main(String[] args) throws IOException {
        File csv = new File("Course.csv");
        Course[] courses;
        try (Scanner in = new Scanner(
                new FileInputStream("Course.dat"), "UTF-8")) {
            courses = readData(in);
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
        for (Course course : courses){
            bw.write(course.totalCapacity + "," + course.courseId + "," + course.prerequisite + "," + course.courseHour
            + "," + course.courseCredit + "," + course.courseName + "," + course.className + "," + course.courseDept);
            bw.newLine();
        }
        bw.close();
    }
    public static Course[] readData(Scanner in){
        int n = in.nextInt();
        in.nextLine();

        Course[] courses = new Course[n];
        for(int i = 0; i < n;i++){
            courses[i] = readCourse(in);
        }return courses;
    }
    public static Course readCourse(Scanner in){
        String line = in.nextLine();
        String[] tokens = line.split("\\|");
        int totalCapacity = Integer.parseInt(tokens[0]);
        String courseid = tokens[1];
        String prerequisite = tokens[2];
        int courseHour = Integer.parseInt(tokens[3]);
        double courseCredit = Double.parseDouble(tokens[4]);
        String courseName = tokens[5];
        String className = tokens[6];
        String courseDept = tokens[7];
        return new Course(totalCapacity, courseid.trim(), prerequisite.trim(), courseHour, courseCredit, courseName.trim(), className.trim(), courseDept.trim());
    }
}
class Course {
    // TODO:

    public int totalCapacity;
    public String courseId;
    public String prerequisite;
    public String teacher;
    public ClassList[] classList;
    public int courseHour;
    public double  courseCredit;
    public String courseName;
    public String className;
    public String courseDept;

    Course(int totalCapacity, String courseId, String prerequisite, int courseHour, double courseCredit, String courseName, String className,
           String courseDept){
        this.totalCapacity = totalCapacity;
        this.courseId = courseId;
        this.prerequisite = prerequisite;
        this.courseHour = courseHour;
        this.courseCredit = courseCredit;
        this.courseName = courseName;
        this.className = className;
        this.courseDept = courseDept;

    }
    public String getCourseId(){
        return this.courseId;
    }
    public void setCourseId(String courseId){
        this.courseId = courseId;
    }
    public String getPrerequisite(){
        return this.prerequisite;
    }
    public void setPrerequisite(String prerequisite){
        this.prerequisite = prerequisite;
    }
    public String getTeacher(){
        return this.teacher.trim();
    }
    public void setTeacher(String teacher){
        this.teacher = teacher;
    }
    public String getCourseName(){
        return this.courseName;
    }
    public void setCourseName(String courseName){
        this.courseName = courseName;
    }
    public String getCourseDept(){
        return this.courseDept;
    }
    public void setCourseDept(String courseDept){
        this.courseDept = courseDept;
    }
    public ClassList[] getClassList(){
        return this.classList;
    }
    public void setClassList(ClassList[] classList){
        this.classList = classList;
    }
    public int getTotalCapacity(){
        return this.totalCapacity;
    }
    public void setTotalCapacity(int totalCapacity){
        this.totalCapacity = totalCapacity;
    }

    public int getCourseHour(){
        return courseHour;
    }
    public void setCourseHour(int courseHour){
        this.courseHour = courseHour;
    }
    public double getCourseCredit(){
        return this.courseCredit;
    }
    public void setCourseCredit(double courseCredit){
        this.courseCredit = courseCredit;
    }
    public String getClassName(){
        return this.className;
    }
    public void setClassName(String className){
        this.className = className;
    }

}
class ClassList {
    // TODO: define data-class as the json structure

    public int[] weekList;
    public String location;
    public String classTime;
    public int weekday;
}

