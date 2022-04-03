package Implementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.service.UserService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class UserServiceImplementation implements UserService {
    @Override
    public void removeUser(int userId) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from remove_User(?)")//done test
        ) {
            stmt.setInt(1, userId);
            stmt.execute();
        } catch (SQLException e) {
            throw new EntityNotFoundException();
        }
    }

    @Override
    public List<User> getAllUsers() {
        ResultSet resultSet;
        List<User> result = new ArrayList<>();
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from get_all_Users()")
        ) {
            resultSet = stmt.executeQuery();
            while (resultSet.next()){
                String majorId = resultSet.getString("MajorId");
                if (majorId != null){
                    Student student = new Student();
                    student.id = resultSet.getInt("userId");
                    student.fullName = resultSet.getString("fullName");
                    student.enrolledDate = resultSet.getDate("enrolledDate");
                    Major major = new Major();
                    major.id = resultSet.getInt("MajorId");//done add correspondence column id
                    major.name = resultSet.getString("major_name");//done add correspondence column name
                    major.department = new Department();
                    major.department.id = resultSet.getInt("department_id");//done add correspondence column id
                    major.department.name = resultSet.getString("department_name");//done add correspondence column name
                    student.major = major;
                    result.add(student);
                }else{
                    Instructor instructor = new Instructor();
                    instructor.id = resultSet.getInt("userId");
                    instructor.fullName = resultSet.getString("fullName");
                    result.add(instructor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result.isEmpty())return List.of();
        return result;
    }

    @Override
    public User getUser(int userId) {
        ResultSet resultSet;
        User result = null;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select * from get_User(?)")//todo
        ){
            stmt.setInt(1,userId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String majorId = resultSet.getString("MajorId");
                if (majorId != null) {
                    Student student = new Student();
                    student.id = resultSet.getInt("userId");
                    student.fullName = resultSet.getString("fullName");
                    student.enrolledDate = resultSet.getDate("enrolledDate");
                    Major major = new Major();
                    major.id = resultSet.getInt("MajorId");//done add correspondence column id
                    major.name = resultSet.getString("major_name");//done add correspondence column name
                    major.department = new Department();
                    major.department.id = resultSet.getInt("department_id");//done add correspondence column id
                    major.department.name = resultSet.getString("department_name");//done add correspondence column name
                    student.major = major;
                    result = student;
                } else {
                    Instructor instructor = new Instructor();
                    instructor.id = resultSet.getInt("userId");
                    instructor.fullName = resultSet.getString("fullName");
                    result = instructor;
                }
            }
        }catch (SQLException e){
            throw new EntityNotFoundException();
        }
        return result;
    }
}
