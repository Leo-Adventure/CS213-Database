import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
public class file {
    public static void main(String[] args) throws IOException {
        ArrayList student_infos = new ArrayList<student_info>();

        File file = new File("C:\\Users\\86181\\Downloads\\data\\select_course.csv");
        Scanner input = new Scanner(file);
        while(input.hasNextLine()){
            String str1 = input.nextLine();
            String[] str_split = str1.split(",");
            student_infos.add(new student_info(str_split));
        }
        for(int i = 0; i < student_infos.size(); ++ i)
        System.out.println(student_infos.get(i));
    }

}
//class student_info{
//    String stu_name ;
//    String gender;
//    String college;
//    int sid;
//    ArrayList<String> course = new ArrayList<>();
//    student_info(String[] str){
//        stu_name = str[0];
//        gender = str[1];
//        college = str[2];
//        sid = Integer.valueOf(str[3]);
//        for(int i = 4; i < str.length; i ++){
//            course.add(str[i]);
//        }
//    }
//    public String toString(){
//        return String.format("The student is %s, %s, %s, %d, %s", stu_name,gender, college, sid, course);
//    }
//
//}
