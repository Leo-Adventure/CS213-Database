
public class Client {

    public static void main(String[] args) {

        long l = System.currentTimeMillis();
        System.out.println(l);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
        }

        System.out.println(System.currentTimeMillis() - l);

        System.exit(0);
        try {
            DataManipulation dm = new DataFactory().createDataManipulation("database");
//            dm.createTable("drop table ahh");
//            dm.addOneMovie("流浪地球;cn;2019;127");
//            System.out.println(dm.allContinentNames());
//            System.out.println(dm.continentsWithCountryCount());
//            System.out.println(dm.FullInformationOfMoviesRuntime(65, 75));
//           System.out.println(dm.findMovieById(11));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}

