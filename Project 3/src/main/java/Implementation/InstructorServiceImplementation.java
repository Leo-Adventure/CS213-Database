package Implementation;

import javax.annotation.ParametersAreNonnullByDefault;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.CourseSection;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.InstructorService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
public class InstructorServiceImplementation implements InstructorService {

    @Override
    public void addInstructor(int userId, String firstName, String lastName) {//todo convert function to procedure
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("select add_Instructor(?,?,?,?)")//done todo test
        ) {
            String fullName = firstName + " " + lastName;
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            Matcher m = p.matcher(fullName);
            if (m.find()) {
                fullName = fullName.replaceAll(" ", "");
            }
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, fullName);
            stmt.execute();
        } catch (SQLException e) {
            throw new IntegrityViolationException();
        }
    }

}
