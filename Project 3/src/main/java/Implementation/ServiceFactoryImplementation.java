package Implementation;

import cn.edu.sustech.cs307.factory.ServiceFactory;
import cn.edu.sustech.cs307.service.*;

public class ServiceFactoryImplementation extends ServiceFactory {
    public ServiceFactoryImplementation(){
        registerService(CourseService.class, new CourseServiceImplementation());
        registerService(DepartmentService.class, new DepartmentServiceImplementation());
        registerService(InstructorService.class, new InstructorServiceImplementation());
        registerService(MajorService.class, new MajorServiceImplementation());
        registerService(SemesterService.class, new SemesterServiceImplementation());
        registerService(StudentService.class, new StudentServiceImplementation());
        registerService(UserService.class, new UserServiceImplementation());
    }
}
