package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;
import com.impossibl.jdbc.spy.ConnectionRelay;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.Type;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
public class StudentServiceImplementation implements StudentService {

    private static final Grade.Cases<String> parser = new Grade.Cases<>() {

        @Override
        public String match(PassOrFailGrade self) {
            return self.name();
        }

        @Override
        public String match(HundredMarkGrade self) {
            return String.valueOf(self.mark);
        }
    };

    // to do
    @Override
    public void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate) {
        String fullName = firstName + " " + lastName;
        try (Connection postgreConnection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstm = postgreConnection.prepareStatement("INSERT INTO \"User\" (\"id\", \"fullName\") VALUES(?,?);"
             )) {
            pstm.setInt(1, userId);
            pstm.setString(2, fullName);
            pstm.execute();

            String tableName = "Student";
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            Matcher m = p.matcher(fullName);
            if (m.find()) {
                fullName = fullName.replaceAll(" ", "");
            }

            PreparedStatement sqlCommand = postgreConnection.prepareStatement("INSERT INTO \"" +
                    tableName + "\" (\"userId\", \"majorId\", \"firstName\", \"lastName\", \"enrolledDate\")" +
                    " VALUES (?, ?, ?, ?, ?);");


            sqlCommand.setInt(1, userId);
            sqlCommand.setInt(2, majorId);
            sqlCommand.setString(3, firstName);
            sqlCommand.setString(4, lastName);
            sqlCommand.setDate(5, enrolledDate);


            sqlCommand.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }
    }


    final String replace = "X";
    final String original = "-";

    // to do
    @Override
    public List<CourseSearchEntry> searchCourse(int studentId, int semesterId,
                                                @Nullable String searchCid,
                                                @Nullable String searchName,
                                                @Nullable String searchInstructor,
                                                @Nullable DayOfWeek searchDayOfWeek,
                                                @Nullable Short searchClassTime,
                                                @Nullable List<String> searchClassLocations,
                                                CourseType searchCourseType,
                                                boolean ignoreFull, boolean ignoreConflict,
                                                boolean ignorePassed, boolean ignoreMissingPrerequisites,
                                                int pageSize, int pageIndex) {
        CourseSearchEntry entry = new CourseSearchEntry();

        Set<CourseSectionClass> set = new HashSet<>();
        List<String> conflictCourseNames = new ArrayList<>();

        String dayOfWeek = (searchDayOfWeek == null) ? null : String.valueOf(searchDayOfWeek);
        String courseType = (searchDayOfWeek == null) ? null : String.valueOf(searchCourseType);
        String firstName = "";
        String lastName = "";
        String fullName = "";
        ResultSet totalrs;
        ResultSet conflict_rs;
        int sectionId = 0;
        List<CourseSearchEntry> courseSearchEntries = new ArrayList<>();


        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement("select * from search_course(?, ?, ? , ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?);");
             PreparedStatement preparedStatement = connection.prepareStatement("select * from get_conflictcoursenames(?,?)")) {
            String[] locations = null;
            Array locationArray = null;

            if (searchClassLocations != null) {
                locations = searchClassLocations.toArray(new String[searchClassLocations.size()]);
                locationArray = connection.createArrayOf("varchar", locations);
            }
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, semesterId);
            pstmt.setString(3, searchCid);
            pstmt.setString(4, searchName);
            pstmt.setString(5, searchInstructor);
            pstmt.setString(6, dayOfWeek);
            if (searchClassTime == null) {
                pstmt.setNull(7, Types.SMALLINT);
            } else
                pstmt.setShort(7, searchClassTime);

            pstmt.setArray(8, locationArray);
            pstmt.setString(9, courseType);
            pstmt.setBoolean(10, ignoreFull);
            pstmt.setBoolean(11, ignoreConflict);
            pstmt.setBoolean(12, ignorePassed);
            pstmt.setBoolean(13, ignoreMissingPrerequisites);
            pstmt.setInt(14, pageSize);
            pstmt.setInt(15, pageIndex);

            totalrs = pstmt.executeQuery();
            boolean First = true;
            Course lastTimeCourse = null;
            CourseSection lastTimeCourseSection = null;
            while (totalrs.next()) {
                Course course = new Course();
                Instructor instructor = new Instructor();
                CourseSection courseSection = new CourseSection();
                CourseSectionClass courseSectionClass = new CourseSectionClass();
                course.id = totalrs.getString(1);
                course.name = totalrs.getString(2);
                course.credit = totalrs.getInt(3);
                course.classHour = totalrs.getInt(4);
                course.grading = Course.CourseGrading.valueOf(totalrs.getString(5));

                courseSection.id = totalrs.getInt(6);
                sectionId = courseSection.id;
                courseSection.name = totalrs.getString(7);
                courseSection.leftCapacity = totalrs.getInt(8);
                courseSection.totalCapacity = totalrs.getInt(9);

                if(First){
                    First = false;
                    lastTimeCourse = course;
                    lastTimeCourseSection = courseSection;
                }

                if(courseSection != lastTimeCourseSection){

                    preparedStatement.setInt(1, studentId);
                    preparedStatement.setInt(2, sectionId);
                    conflict_rs = preparedStatement.executeQuery();
                    while(conflict_rs.next()){
                        String name;
                        name = String.format("%s[%s]", conflict_rs.getString(1),
                                conflict_rs.getString(2));
                        conflictCourseNames.add(name);
                    }
                    entry.course = lastTimeCourse;
                    entry.section = lastTimeCourseSection;
                    entry.sectionClasses = set;
                    entry.conflictCourseNames = conflictCourseNames;
                    courseSearchEntries.add(entry);
                    entry = new CourseSearchEntry();
                    conflictCourseNames.clear();
                    set.clear();
                    lastTimeCourseSection = courseSection;
                }
                courseSectionClass.id = totalrs.getInt(10);
                instructor.id = totalrs.getInt(11);
                firstName = totalrs.getString(12);
                lastName = totalrs.getString(13);
                if ((lastName.matches(".*[A-Za-z]+.*") || lastName.equals(" ")) && (firstName.matches(".*[A-Za-z]+.*") || lastName.matches(" "))) {
                    fullName = (firstName + " " + lastName);
                } else
                    fullName = firstName + lastName;
                instructor.fullName = fullName;
                courseSectionClass.instructor = instructor;
                courseSectionClass.dayOfWeek = DayOfWeek.valueOf(totalrs.getString(14));
                Object weekListTemp =  totalrs.getArray("weekList").getArray();
                short[] weekListShort = (short[]) weekListTemp;
                Set<Short> weekListSet = new HashSet<>();
                for(short i : weekListShort)
                    weekListSet.add(i);
                courseSectionClass.weekList = weekListSet;
                courseSectionClass.classBegin = totalrs.getShort(16);
                courseSectionClass.classEnd = totalrs.getShort(17);
                courseSectionClass.location = totalrs.getString(18);
                set.add(courseSectionClass);

            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return courseSearchEntries;
    }

    // to do
    int tot;

    @Override
    public EnrollResult enrollCourse(int studentId, int sectionId) {

        EnrollResult result = EnrollResult.UNKNOWN_ERROR;//TODO

        ResultSet rs;
        ResultSet time_conflict_rs;
        ResultSet pre_rs;
        ResultSet check_rs;

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement("select * from enroll_course(?,?)")) {

            /*PreparedStatement course_not_found_psmt = connection.prepareStatement("SELECT \"sectionId\" from \"CourseSection\"; ");
            PreparedStatement already_enrolled_psmt = connection.prepareStatement("SELECT \"studentId\", \"sectionId\", \"grade\" FROM \"student_section\";");
            PreparedStatement passed_psmt = connection.prepareStatement("SELECT \"studentId\", \"sectionId\", \"grade\" FROM \"student_section\";");)//done
            boolean courseFound = false;
            course_not_found_rs = course_not_found_psmt.executeQuery();
            int section_id = 0;
            while (course_not_found_rs.next()) {
                section_id = course_not_found_rs.getInt(1);
                if (sectionId == section_id)
                    courseFound = true;
            }
            if (!courseFound)
                return EnrollResult.COURSE_NOT_FOUND;

            //判断是否重复选课(无成绩)
            int stu_id, sec_id;
            String grade1;
            boolean already_enrolled = false;
            already_enrolled_rs = already_enrolled_psmt.executeQuery();
            while (course_not_found_rs.next()) {
                stu_id = already_enrolled_rs.getInt(1);
                sec_id = already_enrolled_rs.getInt(2);
                grade1 = already_enrolled_rs.getString(3);
                if (stu_id == studentId && sec_id == sectionId && grade1 == null)
                    already_enrolled = true;
            }
            if (already_enrolled)
                return EnrollResult.ALREADY_ENROLLED;

            //判断是否已经通过此课
            boolean passed = false;
            String grade = "";
            passed_rs = passed_psmt.executeQuery();
            while (passed_rs.next()) {
                stu_id = passed_rs.getInt(1);
                sec_id = passed_rs.getInt(2);
                grade = passed_rs.getString(3);
                if (stu_id == studentId && sec_id == sectionId) {
                    if (grade.equals("PASS"))
                        passed = true;
                    else if (Integer.parseInt(grade) >= 60)
                        passed = true;
                }
                if (passed)
                    return EnrollResult.ALREADY_PASSED;
*/
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) == 1) return EnrollResult.COURSE_NOT_FOUND;
                else if (rs.getInt(1) == 2) return EnrollResult.ALREADY_ENROLLED;
                else if (rs.getInt(1) == 3) return EnrollResult.ALREADY_PASSED;
                else if (rs.getInt(1) == 5) return EnrollResult.COURSE_CONFLICT_FOUND;
                else if (rs.getInt(1) == 6) return EnrollResult.COURSE_IS_FULL;
            }

            //判断是否满足先修课
            ArrayList<String> courseidFulfillList = new ArrayList<>();
            ArrayList<String> courseTotalList = new ArrayList<>();
            String courseId;
            PreparedStatement prerequisite_psmt = connection.prepareStatement(" SELECT courseid FROM get_course_prerequisite(?,?);");
            PreparedStatement check_prerequisite_psmt = connection.prepareStatement("SELECT * FROM passed_prerequisites_for_course(?,?,null,null);");
            prerequisite_psmt.setInt(1, studentId);
            prerequisite_psmt.setInt(2, sectionId);
            pre_rs = prerequisite_psmt.executeQuery();
            while (pre_rs.next()) {
                courseId = pre_rs.getString(1);
                if (!courseTotalList.contains(courseId)) {
                    courseTotalList.add(courseId);
                }
                check_prerequisite_psmt.setInt(1, studentId);
                check_prerequisite_psmt.setString(2, courseId);
                check_rs = check_prerequisite_psmt.executeQuery();
                boolean fulfilled = check_rs.getBoolean(1);
                if (fulfilled) {
                    courseidFulfillList.add(courseId);
                }
                if (courseidFulfillList.size() < courseTotalList.size()) {
                    return EnrollResult.PREREQUISITES_NOT_FULFILLED;
                }
            }

            //判断课程是否冲突

            PreparedStatement time_conflict_pstmt = connection.prepareStatement("SELECT * FROM get_course_time_conflict(?,?)");
            //PreparedStatement weeklist_conflict_pstmt = connection.prepareStatement("SELECT \"weekList\" from get_weeklist(?,?);");
            //PreparedStatement other_weeklist_conflict_pstmt = connection.prepareStatement("SELECT \"weekList\" from get_weeklist(?,?);");
//            weeklist_conflict_pstmt.setInt(1, studentId);
//            weeklist_conflict_pstmt.setInt(2, sectionId);

            time_conflict_pstmt.setInt(1, studentId);
            time_conflict_pstmt.setInt(2, sectionId);

            time_conflict_rs = time_conflict_pstmt.executeQuery();
            if (time_conflict_rs.next())
                return EnrollResult.COURSE_CONFLICT_FOUND;

            //时间冲突
//            while (time_conflict_rs.next()) {
//                int conflict_sectionId = time_conflict_rs.getInt(1);
////                other_weeklist_conflict_pstmt.setInt(1, studentId);
////                other_weeklist_conflict_pstmt.setInt(2, conflict_sectionId);
//
////                if (checkWeekList(weeklist_conflict_pstmt, other_weeklist_conflict_pstmt))
////                    return EnrollResult.COURSE_CONFLICT_FOUND;
//            }
/*
            //判读课程是否已经选满
            boolean isFull = false;
            PreparedStatement full_conflict_psmt = connection.prepareStatement("SELECT \"totalCapacity\", \"leftCapacity\" FROM \"CourseSection\" WHERE \"sectionId\" = ?;");

            full_conflict_psmt.setInt(1, sectionId);
            full_rs = full_conflict_psmt.executeQuery();
            while (full_rs.next()) {
                int total = full_rs.getInt(1);
                int left = full_rs.getInt(2);
                if (total <= left)
                    return EnrollResult.COURSE_IS_FULL;

            }
*/

            PreparedStatement success_pstm = connection.prepareStatement("update \"CourseSection\" set \"leftCapacity\" = \"leftCapacity\" - 1 where \"sectionId\" = ?");
            success_pstm.setInt(1, sectionId);
            success_pstm.execute();
            result = EnrollResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            result = EnrollResult.UNKNOWN_ERROR;
            throw new IntegrityViolationException();
        }
        return result;


    }

//    private boolean checkWeekList(PreparedStatement ori, PreparedStatement other) throws SQLException {
//        ResultSet week1, week2;
//        week1 = ori.executeQuery();
//        week2 = other.executeQuery();
//        ArrayList<Short> week1_list = (ArrayList<Short>) week1.getObject(1);
//        ArrayList<Short> week2_list = (ArrayList<Short>) week2.getObject(1);
//
//        for (Short week : week1_list) {
//            if (week2_list.contains(week))
//                return true;
//        }
//        return false;
//    }

    // to do

    @Override
    public void dropCourse(int studentId, int sectionId) {
        ResultSet check_rs;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM dropCourse(?,?)")// done,todo no error given
        ) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            PreparedStatement checkpstmt = connection.prepareStatement("SELECT grade FROM \"student_section\" where \"studentId\" = ? and \"sectionId\" = ?;");
            checkpstmt.setInt(1, studentId);
            checkpstmt.setInt(2, sectionId);
            check_rs = checkpstmt.executeQuery();
            while (check_rs.next()) {
                String grade = check_rs.getString(1);
                if (grade != null) {
                    throw new IllegalStateException();
                }
            }
            stmt.execute();
            //进行剩余容量的调整
            PreparedStatement left_chage_pstmt = connection.prepareStatement("update \"CourseSection\" set \"leftCapacity\" = \"leftCapacity\" + 1 where \"sectionId\" = ?");
            left_chage_pstmt.setInt(1, studentId);
            left_chage_pstmt.setInt(2, sectionId);
            left_chage_pstmt.execute();
        } catch (SQLException e) {
//            e.printStackTrace();
//            throw new EntityNotFoundException();
        }
    }

    // to do
    @Override
    public void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade) {
        // to do

        ResultSet resultSet;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("insert into student_section(\"studentId\", \"sectionId\", \"grade\") VALUES(?,?,?);")//done
        ) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            if (grade != null) {
                if (parserGrade(grade).equals("PASS") || parserGrade(grade).equals("FAIL")) {
                    stmt.setString(3, parserGrade(grade));
                } else if (parserGrade(grade).matches("[0-9]+")) {
                    stmt.setInt(3, Integer.parseInt(parserGrade(grade)));
                } else
                    stmt.setString(3, null);
            } else {
                stmt.setString(3, null);
            }
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }

    }

    // to do
    @Override
    public CourseTable getCourseTable(int studentId, Date date) {

        CourseTable courseTable = new CourseTable();
        courseTable.table = new HashMap<>();
        Set<CourseTable.CourseTableEntry> set_MON = new HashSet<>();
        Set<CourseTable.CourseTableEntry> set_TUE = new HashSet<>();
        Set<CourseTable.CourseTableEntry> set_WED = new HashSet<>();
        Set<CourseTable.CourseTableEntry> set_THU = new HashSet<>();
        Set<CourseTable.CourseTableEntry> set_FRI = new HashSet<>();
        Set<CourseTable.CourseTableEntry> set_SAT = new HashSet<>();
        Set<CourseTable.CourseTableEntry> set_SUN = new HashSet<>();

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection()) {
            PreparedStatement stmt1 = connection.prepareStatement("select \"begin\", \"end\", \"id\" from \"Semester\";");
            int semesterId = getSemesterId(date, stmt1);
            PreparedStatement stmt = connection.prepareStatement("SELECT * from \"get_coursetable\" (? ,?);");
            stmt.setInt(1, studentId);
            stmt.setInt(2, semesterId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CourseTable.CourseTableEntry entry = new CourseTable.CourseTableEntry();
                String fullName = String.format("%s[%s]", rs.getString(1), rs.getString(2));
                String location = rs.getString(6);
                String iFullName;
                short classBegin = rs.getShort(7);
                short classEnd = rs.getShort(8);
                Instructor instructor = new Instructor();
                instructor.id = rs.getInt(3);
                String in_firstName = rs.getString(4);
                String in_lastName = rs.getString(5);
                if ((in_lastName.matches(".*[A-Za-z]+.*") || in_lastName.equals(" ")) && (in_firstName.matches(".*[A-Za-z]+.*") || in_lastName.matches(" "))) {
                    iFullName = (in_firstName + " " + in_lastName);
                } else
                    iFullName = in_firstName + in_lastName;

                instructor.fullName = iFullName;
                entry.courseFullName = fullName;
                entry.instructor = instructor;
                entry.classBegin = classBegin;
                entry.classEnd = classEnd;
                entry.location = location;

                String dayOfWeek = rs.getString(9);
                switch (dayOfWeek) {
                    case "MONDAY":
                        set_MON.add(entry);
                        break;
                    case "TUESDAY":
                        set_TUE.add(entry);
                        break;
                    case "WEDNESDAY":
                        set_WED.add(entry);
                        break;
                    case "THURSDAY":
                        set_THU.add(entry);
                        break;
                    case "FRIDAY":
                        set_FRI.add(entry);
                        break;
                    case "SATURDAY":
                        set_SAT.add(entry);
                        break;
                    case "SUNDAY":
                        set_SUN.add(entry);
                        break;
                }
            }

            courseTable.table.put(DayOfWeek.MONDAY, set_MON);
            courseTable.table.put(DayOfWeek.TUESDAY, set_TUE);
            courseTable.table.put(DayOfWeek.WEDNESDAY, set_WED);
            courseTable.table.put(DayOfWeek.THURSDAY, set_THU);
            courseTable.table.put(DayOfWeek.FRIDAY, set_FRI);
            courseTable.table.put(DayOfWeek.SATURDAY, set_SAT);
            courseTable.table.put(DayOfWeek.SUNDAY, set_SUN);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return courseTable;


    }

    private ArrayList<String> getCourseFULLName(int studentId, int semesterId) throws SQLException {
        ArrayList<String> courseFullNames = new ArrayList<>();
        Connection connection = SQLDataSource.getInstance().getSQLConnection();
        PreparedStatement stmt = connection.prepareStatement("SELECT cs.\"sectionName\", \"courseName\" FROM \"CourseSection\"  cs join \"student_section\" as ss on cs.\"sectionId\" = ss.\"sectionId\" join \"Course\" cour on cour.\"courseId\" = cs.\"courseId\"\n" +
                "where ss.\"studentId\" = ? and cs.\"semesterId\" = ?;");//done
        stmt.setInt(1, studentId);
        stmt.setInt(2, semesterId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String sectionName = rs.getString(1);
            String courseName = rs.getString(2);
            String fullName = String.format("%s[%s]", courseName, sectionName);
            courseFullNames.add(fullName);
        }
        return courseFullNames;
    }

    private int getSemesterId(Date currentDate, PreparedStatement stmt) throws SQLException {

        ResultSet rs = stmt.executeQuery();
        int semesterId = -1;
        while (rs.next()) {
            Date date_begin = rs.getDate(1);
            Date date_end = rs.getDate(2);

            Calendar cal_begin = Calendar.getInstance();
            cal_begin.setTime(date_begin);
            Calendar cal_end = Calendar.getInstance();
            cal_end.setTime(date_end);
            Calendar cal_now = Calendar.getInstance();
            cal_now.setTime(currentDate);
            if (cal_now.after(cal_begin) && cal_now.before(cal_end)) {
                semesterId = rs.getInt(3);
                break;
            }
        }
        return semesterId;
    }


    private static String parserGrade(Grade grade) {
        return grade == null ? null : grade.when(parser);
    }

}
