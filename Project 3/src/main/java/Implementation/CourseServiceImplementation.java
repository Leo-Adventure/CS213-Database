package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;
import com.impossibl.postgres.jdbc.ArrayUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.sql.rowset.serial.SerialArray;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;

@ParametersAreNonnullByDefault
public class CourseServiceImplementation implements CourseService {

    final String replace = "X";

    @Override
    public void addCourse(String courseId, String courseName,
                          int credit, int classHour,
                          Course.CourseGrading grading,
                          @Nullable Prerequisite coursePrerequisite) {

        courseId = courseId.replace("-", replace);
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select add_Course(?,?,?,?,?)")//todo prerequisite
        ) {


            stmt.setString(1, courseId);
            stmt.setString(2, courseName);
            stmt.setInt(3, credit);
            stmt.setInt(4, classHour);
            stmt.setString(5, grading.name());
            stmt.execute();
            if (coursePrerequisite != null) {
                try (
                        PreparedStatement prerequisite =
                                connection.prepareStatement("insert into prerequisite (\"courseId\", path, level, \"No\") values (?,text2ltree(?),?,?)")
                ) {
                    String path = "Top." + courseId;
                    prerequisite.setString(1, courseId);
                    prerequisite.setString(2, path);
                    prerequisite.setInt(3, 2);
                    addPrerequisite(prerequisite, coursePrerequisite, "Top." + courseId, courseId, 2, 1);
                    prerequisite.executeBatch();
                }
            }

        } catch (SQLException e) {
            System.out.println(courseId);
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    // done
    public void addPrerequisite(PreparedStatement preparedStatement,
                                Prerequisite prerequisite,
                                String path,
                                String courseId,
                                int level,
                                int no) {
        courseId = courseId.replace("-", replace);
        if (prerequisite == null) return;
        level = level + 1;
        if (prerequisite instanceof AndPrerequisite) {
            path += (".and" + no);
            int i = 1;
            try {
                preparedStatement.setString(1, courseId);
                preparedStatement.setString(2, path);
                preparedStatement.setInt(3, level);
                preparedStatement.setInt(4, no);
                preparedStatement.addBatch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            for (Prerequisite tmp : ((AndPrerequisite) prerequisite).terms) {
                addPrerequisite(preparedStatement, tmp, path, courseId, level, i);
                i++;
            }
        } else if (prerequisite instanceof OrPrerequisite) {
            path += (".or" + no);
            int i = 1;
            try {
                preparedStatement.setString(1, courseId);
                preparedStatement.setString(2, path);
                preparedStatement.setInt(3, level);
                preparedStatement.setInt(4, no);
                preparedStatement.addBatch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            for (Prerequisite tmp : ((OrPrerequisite) prerequisite).terms) {
                addPrerequisite(preparedStatement, tmp, path, courseId, level, i);
                i++;
            }
        } else if (prerequisite instanceof CoursePrerequisite) {
            path += ("." + ((CoursePrerequisite) prerequisite).courseID);
            try {
                preparedStatement.setString(1, courseId);
                preparedStatement.setString(2, path);
                preparedStatement.setInt(3, level);
                preparedStatement.setInt(4, no);
                preparedStatement.addBatch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    // to do
    @Override
    synchronized
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {

        int result = 0;
        ResultSet resultSet;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from add_coursesection(?,?,?,?,?)")//done
        ) {
            stmt.setString(1, courseId);
            stmt.setInt(2, semesterId);
            stmt.setString(3, sectionName);
            stmt.setInt(4, totalCapacity);
            stmt.setInt(5, totalCapacity);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                result = resultSet.getInt("add_coursesection");
            }

            /*if(!repeat) {
                System.out.println("name = " + Thread.currentThread().getName() + "\tcourseId = " + courseId + "\n");
                course_stmt.setString(1, courseId);
                course_stmt.execute();
                stmt.setString(1, courseId);
                stmt.setInt(2, semesterId);
                stmt.setString(3, sectionName);
                stmt.setInt(4, totalCapacity);
                stmt.setInt(5, totalCapacity);

            }*/
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
        return result;
    }

    // to do
    @Override
    synchronized
    public int addCourseSectionClass(int sectionId, int instructorId,
                                     DayOfWeek dayOfWeek, Set<Short> weekList,
                                     short classStart, short classEnd,
                                     String location) {
        ResultSet change_rs;
        ResultSet rs;
        int result = 0;
        boolean canInsert = false;
        ArrayList<Short> weekList_Arraylist = new ArrayList<>();

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
        ) {
            /* Short[] week = new Short[weekList.size()];
           weekList.toArray(week);
           short[] week_int = new short[weekList.size()];
           for(int i = 0; i < week.length; i++){
               week_int[i] = week[i];
               System.out.println(week_int[i]);
           }*/
            for (Short weekSet : weekList) {
                weekList_Arraylist.add(weekSet);
            }
            Short[] weeklist = new Short[weekList_Arraylist.size()];
            for (int i = 0; i < weeklist.length; i++) {
                weeklist[i] = weekList_Arraylist.get(i);
            }
            String dayOfWeek_Upper = dayOfWeek.name().toUpperCase();
            dayOfWeek = DayOfWeek.valueOf(dayOfWeek_Upper);
            String s = "(SELECT (CAST (ARRAY " + Arrays.toString(weeklist) + " AS SMALLINT[])))";
            String str = String.format("select * from add_coursesectionclass(?,?,?,%s,?,?,?);", s);
//            Array array_weeklist =  connection.createArrayOf("smallint[]", weeklist);
            PreparedStatement acsc = connection.prepareStatement(str);
            acsc.setInt(1, sectionId);
            acsc.setInt(2, instructorId);
            acsc.setString(3, String.valueOf(dayOfWeek));
            acsc.setShort(4, classStart);
            acsc.setShort(5, classEnd);
            acsc.setString(6, location);
//            System.out.println(str);
            rs = acsc.executeQuery();
            while (rs.next()) {
                result = rs.getInt("add_coursesectionclass");
            }
            /*PreparedStatement check_pstmt = connection.prepareStatement("SELECT \"sectionId\" FROM \"CourseSection\";");
            check_rs = check_pstmt.executeQuery();
            PreparedStatement insert_pstm = connection.prepareStatement("INSERT INTO \"CourseSection\" (\"sectionId\") VALUES(?);");
            insert_pstm.setInt(1, sectionId);

            while(check_rs.next()){
                int section = check_rs.getInt(1);
                if(!sectionIdList.contains(section)) {
                    sectionIdList.add(section);
                    insert_pstm.execute();
                }
            }*/

            /*String cmd = String.format("INSERT INTO \"CourseSectionClass\"(\"sectionId\", \"instructor\", \"dayOfWeek\", \"weekList\", \"classStart\", \"classEnd\", \"location\") VALUES(%s, %s, '%s', (select array %s), %s, %S, '%s');", sectionId, instructorId, dayOfWeek.name().toUpperCase(), weekList, classStart, classEnd, location);
            System.out.println(cmd);
            Statement statement = connection.createStatement();
            statement.executeUpdate(cmd);*/

        } catch (SQLException e) {

            e.printStackTrace();
            throw new IntegrityViolationException();
        }
        return result;
    }


    // to do
    @Override
    public List<Course> getAllCourses() {
        ResultSet resultSet;
        List<Course> result = new ArrayList<>();
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT \"courseId\", \"courseName\", \"credit\", \"classHour\", \"grading\" FROM \"Course\";")//done  change it to function
        ) {
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Course tmp = new Course();
                tmp.id = resultSet.getString("courseId");//done add correspondence column id
                tmp.name = resultSet.getString("courseName");//done add correspondence column name
                tmp.credit = resultSet.getInt("credit");
                tmp.classHour = resultSet.getInt("courseHour");
                tmp.grading = (Course.CourseGrading) resultSet.getObject("grading");
                result.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result.isEmpty()) {
            return List.of();
        }
        return result;
    }
}

