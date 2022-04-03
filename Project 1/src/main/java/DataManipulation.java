import java.util.List;

public interface DataManipulation {

    public int addOneMovie(String str);
    public String allContinentNames();
    public String continentsWithCountryCount();
    public String FullInformationOfMoviesRuntime(int min, int max);
    public String findMovieById(int id);
    public void createTable(String mendatory);
    public void addOneCourseDept(Course course);
    public void addOneCourse(Course course);
    public void addOneTeacher(String teacher);
    public void addOneClassList(Course course, int index);
    public void addOneStudent(List<student_info> student_infos);
    public void addOneCollege(List<student_info> student_info);
    public void addOneTeach(List<Course> courses);
}
