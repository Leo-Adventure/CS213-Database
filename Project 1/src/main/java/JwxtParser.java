import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.xml.crypto.Data;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
// TODO: import the json library of your choice

@SuppressWarnings("all")
public class JwxtParser {

    // Run as: jshell JwxtParser.java <json file>
    public static void main(String[] args) throws Exception {
        ArrayList<student_info> student_infos = new ArrayList();

        File file = new File("C:\\Users\\86181\\Downloads\\data\\select_course.csv");
        File student1 = new File("student1.csv");
        File student_100 = new File("studnet_100.csv");
        File selection_s = new File("selection_ss.csv");
        Scanner input = new Scanner(file);
        while (input.hasNextLine()) {
            String str1 = input.nextLine();
            String[] str_split = str1.split(",");
            student_infos.add(new student_info(str_split));
        }
        ArrayList<String> dups = new ArrayList<>();
        new DatabaseManipulation().addOneStudent(student_infos);

        System.exit(0);
        // new DatabaseManipulation().addSelection(student_infos);
        // for(int i = 0; i < student_infos.size(); ++ i)
        // System.out.println(student_infos.get(i));
        ArrayList<Integer> sids = new ArrayList<>();
        BufferedWriter bw = new BufferedWriter(new FileWriter(student_100, false));
        int size = 0;
        long begin = System.currentTimeMillis();
        for (student_info student_info : student_infos) {
            if (!sids.contains(student_info.sid) && student_info.sid > 0) {
                bw.write(student_info.stu_name + "," + student_info.gender + "," + student_info.college
                        + "," + student_info.sid);
                bw.newLine();
                sids.add(student_info.sid);
                size++;
            }
            // 总共由四百万条数据
        }
        // for (student_info s: student_infos){
        // for(int i = 0; i < s.course.size(); i ++){
        // if(!dups.contains(s.sid + s.course.get(i).trim())){
        // bw.write(s.sid + "," + s.course.get(i).trim());
        // dups.add(s.sid + s.course.get(i).trim());
        // size ++;
        // bw.newLine();
        // if(size % 1000 == 0)
        // System.out.println(size);
        // }
        // }
        // }

        System.out.println("Using " + (System.currentTimeMillis() - begin) + "ms");
        System.out.println(size);
        bw.close();

        System.exit(1);
        args = new String[] { ".\\course_info.json" };
        String content = Files.readString(Path.of(args[0]));
        // Gson is an example
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<List<Course>>() {
        }.getType();

        ArrayList<Course> courses = gson.fromJson(content, type);

        try {
            ArrayList<String> courseDept = new ArrayList<>();
            ArrayList<String> teacher_single = new ArrayList<>();
            ArrayList<String> colleges = new ArrayList<>();
            DataManipulation data = new DataFactory().createDataManipulation("database");
            DataManipulation addDept = new DataFactory().createDataManipulation("database");
            DataManipulation addCourse = new DataFactory().createDataManipulation("database");
            DataManipulation addTeacher = new DataFactory().createDataManipulation("database");
            DataManipulation addClassList = new DataFactory().createDataManipulation("database");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
        // for(Course course: courses){
        // System.out.printf("The courseid is %s\n",course.courseId);
        // }

        try (PrintWriter out = new PrintWriter("Course.dat", "UTF-8")) {
            writeData(courses, out);
        }
    }

    public static void writeCourse(PrintWriter out, Course course) {
        out.println(course.getTotalCapacity() + "|" + course.getCourseId() + "|" + course.getPrerequisite() + "|"
                + course.getCourseHour() + "|"
                + course.getCourseCredit() + "|" + course.getCourseName() + "|" + course.getClassName() + "|"
                + course.getCourseDept());
    }

    private static void writeData(ArrayList<Course> courses, PrintWriter out) {
        out.println(courses.size());

        for (Course course : courses) {
            writeCourse(out, course);
        }
    }
}
// new DatabaseManipulation().addOneTeach(courses);

// new DatabaseManipulation().test(student_infos);
// System.exit(0);

// int i = 1;
// 同时加入course和classlist
// for(Course course:courses){
// addCourse.addOneCourse(course);
// addClassList.addOneClassList(course, i);
// i++;
// }

// new DatabaseManipulation().addOneStudent(student_infos);
// new DatabaseManipulation().test(student_infos);
// DatabaseManipulation_ex addOneStudent = new
// DataFactory().createDataManipulation("database");
// 加入classList
/*
 * for(int i = 0; i < courses.size(); i ++){
 * addClassList.addOneClassList(courses.get(i));
 * }
 */
// System.out.println(Arrays.toString(courses.get(0).classList));
// 加入老师
/*
 * for(int i = 0; i < courses.size(); i++) {
 * if (courses.get(i).teacher == null ) {
 * System.out.println("Empty");
 * if(!teacher_single.contains("null")) {
 * addTeacher.addOneTeacher(null);
 * teacher_single.add("null");
 * }
 * 
 * } else {
 * //courses.get(i).teacher.trim();
 * String[] teacher = courses.get(i).teacher.split(",");
 * 
 * if (teacher.length == 1 && !teacher_single.contains(teacher[0].trim())) {
 * teacher[0] = teacher[0].trim();
 * addTeacher.addOneTeacher(teacher[0]);
 * teacher_single.add(teacher[0]);
 * } else {
 * for (int j = 0; j < teacher.length; j++) {
 * if (!teacher_single.contains(teacher[j].trim())){
 * teacher[j] = teacher[j].trim();
 * addTeacher.addOneTeacher((teacher[j]).trim());
 * teacher_single.add(teacher[j].trim());
 * }
 * 
 * }
 * }
 * }
 * }
 */
// addCourse.addOneCourse(courses.get(i));
// if (!courseDept.contains(courses.get(i).courseDept)) {
// courseDept.add(courses.get(i).courseDept);
// addDept.addOneCourseDept(courses.get(i));
// }
// else continue;

class Course {
    // TODO:

    public int totalCapacity;
    public String courseId;
    public String prerequisite;
    public String teacher;
    public ClassList[] classList;
    public int courseHour;
    public double courseCredit;
    public String courseName;
    public String className;
    public String courseDept;

    public String getCourseId() {
        return this.courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getPrerequisite() {
        return this.prerequisite;
    }

    public void setPrerequisite(String prerequisite) {
        this.prerequisite = prerequisite;
    }

    public String getTeacher() {
        return this.teacher.trim();
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getCourseName() {
        return this.courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseDept() {
        return this.courseDept;
    }

    public void setCourseDept(String courseDept) {
        this.courseDept = courseDept;
    }

    public ClassList[] getClassList() {
        return this.classList;
    }

    public void setClassList(ClassList[] classList) {
        this.classList = classList;
    }

    public int getTotalCapacity() {
        return this.totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getCourseHour() {
        return courseHour;
    }

    public void setCourseHour(int courseHour) {
        this.courseHour = courseHour;
    }

    public double getCourseCredit() {
        return this.courseCredit;
    }

    public void setCourseCredit(double courseCredit) {
        this.courseCredit = courseCredit;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
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

class course_info {
    public int totalCapacity;
    public String courseId;
    public String prerequisite;
    public String teacher;
    public ClassList[] classList;
    public int courseHour;
    public double courseCredit;
    public String courseName;
    public String className;
    public String courseDept;
}

class student_info {
    String stu_name;
    String gender;
    String college;
    int sid;
    ArrayList<String> course = new ArrayList<>();

    student_info(String[] str) {
        stu_name = str[0];
        gender = str[1];
        college = str[2];
        sid = Integer.valueOf(str[3]);
        for (int i = 4; i < str.length; i++) {
            course.add(str[i]);
        }
    }

    public String toString() {
        return String.format("The student is %s, %s, %s, %d, %s", stu_name, gender, college, sid, course);
    }

}
