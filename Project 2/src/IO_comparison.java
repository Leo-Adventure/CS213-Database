import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class IO_comparison {
    public static void main(String[] args) throws IOException {
        long begin = System.currentTimeMillis();
        BufferInputStream(10000000);
        System.out.println("Total Time Cost is " + (System.currentTimeMillis() - begin) + "ms");
        }

    public static void BufferInputStream(int limit ) throws IOException {
        int size = 0;
        try{
            long begin = System.currentTimeMillis();
            File file = new File("select_course.csv");
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buffer = new byte[1];
            while(bis.read(buffer) != -1){
                if(size < limit){
                    size++;
                }
                else break;

            }
            bis.close();
            System.out.println("BufferInputStream uses " + (System.currentTimeMillis() - begin) + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void FileInputStream(){
        try{
            long begin =System.currentTimeMillis();
            File file = new File("select_course.csv");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[512];
            while(fis.read(buffer) != -1){
                System.out.print("1");
            }
            fis.close();
            System.out.println("FileStream uses " + (System.currentTimeMillis() - begin) + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void BufferedReader(){
        try{
            long begin =System.currentTimeMillis();
            File file = new File("select_course.csv");
           BufferedReader br = new BufferedReader(new FileReader(file));

            char[] buffer = new char[512];
            while(br.read(buffer) != -1){
                System.out.print("1");
            }
            br.close();
            System.out.println("BufferReader uses " + (System.currentTimeMillis() - begin)+ "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
