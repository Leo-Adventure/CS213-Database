import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.beans.binding.DoubleExpression;

import javax.xml.stream.Location;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Scanner;

public class textfile {
    public static void main(String[] args) throws IOException {
        Employee[] staff = new Employee[3];

        staff[0] = new Employee("Carl Cracker", 75000, 1987, 12, 15);
        staff[1] = new Employee("Harry Hacker", 50000, 1989, 10, 1);
        staff[2] = new Employee("Tony Tester", 40000,  1990, 3, 15);
         try(PrintWriter out = new PrintWriter("employee.dat", "UTF-8")){
             writeData(staff, out);
         }

    }
    private static void writeData(Employee[] employees, PrintWriter out) throws IOException{
        out.println(employees.length);

        for(Employee e: employees){
            writeEmployee(out, e);
        }
    }
    private static Employee[] readData(Scanner in){
        int n = in.nextInt();
        in.nextLine();

        Employee[] employees = new Employee[n];
        for(int i = 0; i < n; i ++){
            employees[i] = readEmployee(in);
        }
        return employees;

    }
    public static void writeEmployee(PrintWriter out, Employee e){
        out.println(e.getName() + "|" + e.getSalary() + "|" + e.getHireDay() );
    }

    public static Employee readEmployee(Scanner in){
        String line = in.nextLine();
        String[] tokens = line.split("\\|");
        String name = tokens[0];
        double salary = Double.parseDouble(tokens[1]);
        LocalDate hireDate = LocalDate.parse(tokens[2]);
        int year = hireDate.getYear();
        int month = hireDate.getMonthValue();
        int day = hireDate.getDayOfMonth();
        return new Employee(name, salary, year, month, day);
    }
}
@SuppressWarnings("all")
class Employee{
    private String name;
    private double  money;
    private int born_year;
    private int born_month;
    private int born_day;
    Employee(String name, double  money, int born_year, int born_month, int born_day){
        this.name = name;
        this.money = money;
        this.born_year = born_year;
        this.born_month = born_month;
        this.born_day = born_day;
    }
    public String getName(){
        return this.name;
    }
    public double  getSalary(){
        return this.money;
    }
    public String getHireDay(){
        return String.format("%d, %d, %d", this.born_day, this.born_month, this.born_year);
    }
}
