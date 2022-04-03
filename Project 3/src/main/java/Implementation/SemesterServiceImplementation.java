package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.dto.Semester;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
public class SemesterServiceImplementation implements SemesterService {
    @Override
    public int addSemester(String name, Date begin, Date end) {
        int result = 0;
        ResultSet resultSet;

        if (begin.after(end))throw new IntegrityViolationException();
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from add_Semester(?,?,?)")//done
        ) {
            stmt.setString(1, name);
            stmt.setDate(2, begin);
            stmt.setDate(3, end);
            resultSet = stmt.executeQuery();
            while (resultSet.next()){
                result = resultSet.getInt("add_Semester");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void removeSemester(int semesterId) {

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from remove_Semester(?)")//done
        ) {
            stmt.setInt(1, semesterId);
            stmt.execute();
        } catch (SQLException e) {
            throw new EntityNotFoundException();
        }

    }

    @Override
    public List<Semester> getAllSemesters() {
        ResultSet resultSet;
        List<Semester> result = new ArrayList<>();

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from get_all_Semester()")//done
        ) {
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Semester tmp = new Semester();
                tmp.id = resultSet.getInt("id_out");//done add correspondence column id
                tmp.name = resultSet.getString("semester_name");//done add correspondence column name
                tmp.begin = resultSet.getDate("begin_");//done add correspondence column begin
                tmp.end = resultSet.getDate("end_");//done add correspondence column end
                result.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result.isEmpty())return List.of();
        return result;

    }

}
