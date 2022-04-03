import java.io.*;
import java.util.ArrayList;

public class select_comparison {
    public static void main(String[] args) throws IOException {

        BufferedReader(1000000);
    }
    public static void BufferInputStream() throws IOException {
        try{
            long begin =System.currentTimeMillis();
            File file = new File("student1.csv");
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buffer = new byte[65536];
            int cnt = 0;
            while((cnt = bis.read(buffer)) != -1){
                String all = new String(buffer, 0, cnt);
                System.out.print(all);
            }
            bis.close();
            System.out.println("BufferInputStream uses " + (System.currentTimeMillis() - begin) + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void BufferedReader(int limit){
        try{
            int cnt= 0;
            int line = 0;
            long begin =System.currentTimeMillis();
            File file = new File("selection.csv");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            ArrayList<String> dups = new ArrayList<>();
//            char[] buffer = new char[512];
            while((str = br.readLine())!= null){
                String[] strings = str.split(",");
                if(!dups.contains(strings[0].trim() + strings[1].trim()) && cnt < limit){
                    System.out.println(str);
                    cnt ++;
                    line++;
                    dups.add(strings[0].trim() + strings[1].trim());
                }
                if(cnt == limit)
                    break;
            }
            br.close();
            System.out.println("Total tuples: " + line);
            System.out.println("BufferReader uses " + (System.currentTimeMillis() - begin)+ "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
