
import org.postgresql.util.PSQLException;

import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CheckedOutputStream;

public class DatabaseManipulation implements DataManipulation {
    ArrayList<String> colleges = new ArrayList<>();
    private Connection con = null;
    private ResultSet resultSet;

    private String host = "localhost";
    private String dbname = "lab08";
    private String user = "lmq";
    private String pwd = "280492";
    private String port = "5432";


    private void getConnection() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    private void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public int addOneMovie(String str) {
        getConnection();
        int result = 0;
        String sql = "insert into movies (title, country,year_released,runtime) " +
                "values (?,?,?,?)";
        String movieInfo[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, movieInfo[0]);
            preparedStatement.setString(2, movieInfo[1]);
            preparedStatement.setInt(3, Integer.parseInt(movieInfo[2]));
            preparedStatement.setInt(4, Integer.parseInt(movieInfo[3]));
            System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return result;
    }

    public void test(List<student_info> studentInfoList) {
        getConnection();
        int i = 0;
        final int constI = 100;
        try {
            con.setAutoCommit(false);
            String sql = "insert into student(stu_name,gender,college,sid) " +
                    "values (?,?,?,?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            for (student_info student_info : studentInfoList) {
                int result = 0;
                preparedStatement.setString(1, student_info.stu_name);
                preparedStatement.setString(2, student_info.gender);
                preparedStatement.setString(3, student_info.college);
                preparedStatement.setInt(4, student_info.sid);

                preparedStatement.addBatch();
                if (++i % constI == 0) {
                    preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnection();
        }
    }


//    private void test(PreparedStatement preparedStatement,
//                      student_info student_info) throws SQLException {
//        int result = 0;
//        preparedStatement.setString(1, student_info.stu_name);
//        preparedStatement.setString(2, student_info.gender);
//        preparedStatement.setString(3, student_info.college);
//        preparedStatement.setInt(4, student_info.sid);
//
//        preparedStatement.addBatch();
////        System.out.println("result = " + result);
//    }

    @Override
    public void addOneCollege(List<student_info> student_infos) {
        getConnection();
        try {
            con.setAutoCommit(false);

            for (student_info student_info : student_infos) {
                if (!colleges.contains(student_info.college)) {
                    addOneCollege(student_info);
                    colleges.add(student_info.college);
                }
            }

            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void addOneCollege(student_info student_info) throws SQLException {
        int result = 0;
        String sql = "insert into college(col_name) " +
                "values (?)";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, student_info.college);
        result = preparedStatement.executeUpdate();
//        System.out.println("The college added is " + student_info.college);
        System.out.println(preparedStatement + "\n\tr=" + result);

    }

    public void addOneStudent(List<student_info> student_info) {

        long begin = System.currentTimeMillis();
        getConnection();
        int i = 0;
        int counter = 0;
        int bar = 2048;
        StringBuilder sql = new StringBuilder("insert into student(stu_name, gender, college, sid) values ");
        String sql2 = "(?,?,?,?)";
        final int NUMBER_OF_SIZE = 80;
        //循环80次，拼接320个栏子
        for (int cnt = 0; cnt < NUMBER_OF_SIZE; cnt++) {
            if (cnt == NUMBER_OF_SIZE - 1) sql.append(sql2).append(";");
            else sql.append(sql2).append(",");
        }
        PreparedStatement preparedStatement = null;

        try {
            con.setAutoCommit(false);
            int num = 0;
            preparedStatement = con.prepareStatement(sql.toString());
            for (student_info student_info1 : student_info) {
                ++i;
                {
                    num += 4;
                    preparedStatement.setString(num - 3, student_info1.stu_name);
                    preparedStatement.setString(num - 2, student_info1.gender);
                    preparedStatement.setString(num - 1, student_info1.college);
                    preparedStatement.setInt(num, student_info1.sid);
                }
                if (num / 4 % NUMBER_OF_SIZE == 0) {
                    preparedStatement.addBatch();
                    num = 0;
                    counter ++;
                }
                if (counter > 0 && counter % bar == 0) {
                    preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                }
            }
            preparedStatement.executeBatch();
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
            System.out.println((System.currentTimeMillis() - begin)/1000.0);
            System.out.println("The speed is " + i * 1000L / (System.currentTimeMillis() - begin) + "records/s");
        }
    }


    private void addOneStudent(student_info student_info) throws SQLException {
    }

    @Override
    public String allContinentNames() {
        getConnection();
        StringBuilder sb = new StringBuilder();
        String sql = "select continent from countries group by continent";
        try {
            Statement statement = con.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                sb.append(resultSet.getString("continent") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

        return sb.toString();
    }

    @Override
    public String continentsWithCountryCount() {
        getConnection();
        StringBuilder sb = new StringBuilder();
        String sql = "select continent, count(*) countryNumber from countries group by continent;";
        try {
            Statement statement = con.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                sb.append(resultSet.getString("continent") + "\t");
                sb.append(resultSet.getString("countryNumber"));
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        int x ;

        System.out.println(x=2);
        System.out.println(x+=2);
    }


    @Override
    public String FullInformationOfMoviesRuntime(int min, int max) {
        getConnection();
        StringBuilder sb = new StringBuilder();
        String sql = "select m.title,c.country_name country,c.continent ,m.runtime " +
                "from movies m " +
                "join countries c on m.country=c.country_code " +
                "where m.runtime between ? and ? order by runtime;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, min);
            preparedStatement.setInt(2, max);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("runtime") + "\t");
                sb.append(String.format("%-18s", resultSet.getString("country")));
                sb.append(resultSet.getString("continent") + "\t");
                sb.append(resultSet.getString("title") + "\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return sb.toString();
    }

    @Override
    public String findMovieById(int id) {
        return null;
    }

    public void createTable(String str) {
        getConnection();


        try {
//            PreparedStatement preparedStatement = con.prepareStatement(str);
            Statement statement = con.createStatement();
            statement.executeUpdate(str);
            System.out.println("execute!");
            System.out.println(statement.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    //    @Override
//    public void addOneCourse(List<Course> courses){
//        int i = 0;
//        getConnection();
//
//        try {
//            con.setAutoCommit(false);
//           for(Course course: courses){
//               addOneCourse(course);
//               if (++i % 5000 == 0) {
//                   con.commit();
//                   System.out.println(i);
//               }
//           }
//
//           con.commit();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            closeConnection();
//        }
//    }


    public void addOneCourse(Course course) {
        getConnection();
        int i = 0;
//        int batch = 1000;
        try {
            String sql = "insert into course(totalcapacity, courseid,prerequisite,coursehour, coursecredit,coursename,classname,coursedept)  "
                    + "values(?,?,?,?,?,?,?,?);";
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            preparedStatement.setInt(1, course.totalCapacity);
            preparedStatement.setString(2, course.courseId);
            preparedStatement.setString(3, (course.prerequisite == null) ? null : course.prerequisite);
            preparedStatement.setInt(4, course.courseHour);
            preparedStatement.setDouble(5, course.courseCredit);
            preparedStatement.setString(6, course.courseName);
            preparedStatement.setString(7, course.className);
            preparedStatement.setString(8, course.courseDept);
            System.out.println(course.courseId + " " + course.courseDept + " " + course.className + " " + course.courseHour + " " + course.totalCapacity +
                    " " + course.courseName);

            i = preparedStatement.executeUpdate();
//            if(i % batch == 0){
//                System.out.println(i);
//                preparedStatement.executeBatch();
//                preparedStatement.clearBatch();
//
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void addSelection(List<student_info> student_infos) throws SQLException {
        int i = 0;
        int batch = 1000;
        getConnection();
        try {
//           con.setAutoCommit(false);
            String sql = "insert into selection(sid, cid)" + " values(?,?);";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            for (student_info student_info : student_infos) {
                ArrayList<String> course = new ArrayList<>();
                for (int j = 0; j < student_info.course.size(); j++) {
                    if (!course.contains(student_info.course.get(j))) {
                        preparedStatement.setInt(1, student_info.sid);
                        preparedStatement.setString(2, student_info.course.get(j));
                        course.add(student_info.course.get(j));

                        preparedStatement.addBatch();
                        if (i++ % batch == 0) {
                            System.out.println(i);
                            preparedStatement.executeBatch();
                            preparedStatement.clearBatch();
                        }
                    }
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnection();
        }


    }

    public void addOneCourseDept(Course course) {
        getConnection();
        String sql = "insert into courseDept(deptName) " +
                "values (?)";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, course.courseDept);

            int result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

    }

    //TODO:
    public void addOneClass(Course course) {
        getConnection();
        int result = 0;
        String sql = "insert into class() " +
                "values (?,?,?,?,?)";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            String weeklist = "";
            for (int i = 0; i < course.classList[0].weekList.length; i++) {
                if (i == course.classList[0].weekList.length - 1)
                    weeklist += course.classList[0].weekList[i];
                else weeklist = course.classList[0].weekList[i] + ", ";
            }
            preparedStatement.setString(1, weeklist);
            preparedStatement.setString(2, course.className);
            preparedStatement.setString(3, course.classList[1].location);


            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void addOneTeacher(String Teacher) {
        getConnection();
        String sql = "insert into teacher(name) " +
                "values (?)";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, Teacher);
            int result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

//    public void addSelection_Imp(List<student_info> student_infos, List<Course> courses) throws SQLException {
//        getConnection();
//        int cnt = 0;
//        int batch  = 1000;
//        String sql = "insert into selection(sid, cid, coursename, classname) " +
//                "values(?,?,?,?)";
//        PreparedStatement preparedStatement = con.prepareStatement(sql);
//        for(student_info student_info:student_infos){
//            ArrayList<String> coursetol = new ArrayList<>();
//            for(int i = 0;i < student_info.course.size();i++){
//                if(!coursetol.contains(student_info.course.get(i).trim())){
//                    for(Course course: courses) {
//                        ArrayList<String> totalinfos = new ArrayList<>();
//                        String str = student_info.sid + student_info.course.get(i).trim() + course.courseName.trim();
//                        if (!totalinfos.contains(str)) {
//                            if (course.courseId.trim().equals(student_info.course.get(i).trim())) {
//                                preparedStatement.setInt(1, student_info.sid);
//                                preparedStatement.setString(2, student_info.course.get(i).trim());
//                                preparedStatement.setString(3, course.courseName.trim());
//                                preparedStatement.setString(4, course.className.trim());
//                                totalinfos.add(str);
//                                coursetol.add(student_info.course.get(i).trim());
//                                preparedStatement.addBatch();
//
//                                if (++cnt % batch == 0) {
//                                    System.out.println(cnt);
//                                    preparedStatement.executeBatch();
//                                    preparedStatement.clearBatch();
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        preparedStatement.executeBatch();
//        preparedStatement.clearBatch();
//        closeConnection();
//    }

    public void addOneClassList(Course course, int index) {
        getConnection();
        String sql = "insert into classlist(courseindex,classname, weeklist, location, classtime, weekday) " +
                "values (?,?,?,?,?,?)";
        String weeklist = "{";
        if (course.classList.length == 1) {
            for (int i = 0; i < course.classList[0].weekList.length; i++) {
                if (i == 0) weeklist = weeklist + course.classList[0].weekList[i];
                else if (i != course.classList[0].weekList.length - 1) {
                    weeklist = weeklist + ", " + course.classList[0].weekList[i];
                } else weeklist = weeklist + ", " + course.classList[0].weekList[i] + "}";
            }
            try {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, index);
                preparedStatement.setString(2, course.className);
                preparedStatement.setString(3, weeklist);
                course.classList[0].location = course.classList[0].location.trim();
                preparedStatement.setString(4, (course.classList[0].location == "") ? null : course.classList[0].location);
                preparedStatement.setString(5, course.classList[0].classTime);
                preparedStatement.setInt(6, course.classList[0].weekday);
                int result = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        } else {
            for (int j = 0; j < course.classList.length; j++) {
                try {
                    if (con == null || con.isClosed()) {
                        getConnection();
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                String weeklistMUL = "{";
                for (int i = 0; i < course.classList[j].weekList.length; i++) {
                    if (i == 0) weeklistMUL = weeklistMUL + course.classList[j].weekList[i];
                    else if (i != course.classList[j].weekList.length - 1) {
                        weeklistMUL = weeklistMUL + ", " + course.classList[j].weekList[i];
                    } else weeklistMUL = weeklistMUL + ", " + course.classList[j].weekList[i] + "}";
                }
                try {
                    PreparedStatement preparedStatement = con.prepareStatement(sql);
                    //System.err.println(weeklist.length());
                    preparedStatement.setInt(1, index);
                    preparedStatement.setString(2, course.className);
                    preparedStatement.setString(3, weeklistMUL);
                    course.classList[j].location = course.classList[j].location.trim();
                    preparedStatement.setString(4, (course.classList[j].location == "") ? null : course.classList[0].location);
                    preparedStatement.setString(5, course.classList[j].classTime);
                    preparedStatement.setInt(6, course.classList[j].weekday);
                    int result = preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }
        }
    }

    @Override
    public void addOneTeach(List<Course> courses) {
        getConnection();
        int result = 0;
        ArrayList<String> course_info = new ArrayList<>();
        int cnt = 0;
        int batch = 1000;
        String sql = "insert into teach(teacher_name, course_name, class_name, courseid)" +
                " values (?,?,?,?)";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            for (Course course : courses) {
                if (course.teacher == null) {
                    String cour = "null" + course.courseName.trim() +
                            course.className.trim() + course.courseId.trim();
                    if (!course_info.contains(cour)) {
                        preparedStatement.setString(1, null);
                        preparedStatement.setString(2, course.courseName.trim());
                        preparedStatement.setString(3, course.className.trim());
                        preparedStatement.setString(4, course.courseId.trim());
                        course_info.add(cour);


                    }
                } else {
                    String[] teachers = course.teacher.split(",");
                    for (int i = 0; i < teachers.length; i++) {
                        String cour = (teachers[i] == null) ? "null" : teachers[i].trim() + course.courseName.trim() +
                                course.className.trim() + course.courseId.trim();
                        if (!course_info.contains(cour)) {
                            preparedStatement.setString(1, teachers[i].trim());
                            preparedStatement.setString(2, course.courseName.trim());
                            preparedStatement.setString(3, course.className.trim());
                            preparedStatement.setString(4, course.courseId.trim());
                            course_info.add(cour);
                            result = preparedStatement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnection();
        }
    }

}
