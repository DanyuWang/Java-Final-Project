import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Zhan Yanpeng
 */
public class Crawler {

    /**
     * connect to website, get website's source codes
     * @param url website's url
     * @return
     */
    public  Document getDocument (String url){
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Description About webcrawler:<br>
     * Network crawler method<br>
     * Crawling data from a website,then store the data to the database.<br>
     * @throws ClassNotFoundException if no class
     * @throws SQLException sql connection problem
     */
    public static int webcrawler() throws ClassNotFoundException, SQLException{
        int count = 0;
        String url = "https://www.emsc-csem.org/Earthquake/?view=";
        Class.forName("org.sqlite.JDBC");
        /**
         * connect to database
         * @param conn connect
         */

        Connection conn = DriverManager.getConnection("jdbc:sqlite:/Users/wangholly/IdeaProjects/Pro1/src/earthquakes-1.sqlite");
        conn.setAutoCommit(false);
        Statement stmt =conn.createStatement();
        /**
         * stopDate : Decide when to stop crawling data
         * @param stopDate
         */
        String stopDate="632500";
        ResultSet re2 = stmt.executeQuery("select max(UTC_date) from quakes");
        if(re2.next()){
            stopDate = re2.getString(1);
        }
        ArrayList<String> array = new ArrayList<String>();
        boolean stop=false;
        /**
         * Crawl web site data page by page,until get stopDate.
         */
        for(int i=1;i<70;i++){
            url=url.substring(0, 43)+Integer.toString(i);
            Crawler t = new Crawler();
            Document doc = t.getDocument(url);
            Elements table = doc.select("[id=tbody]");
            Elements rows = table.select("tr");
            for (Element tabletext: rows) {
                Elements cells = tabletext.select("td");
                if (cells.size() > 0) {
                    Elements eeedatetimet = cells.select("b");
                    Elements eedatetimet = eeedatetimet.select("a");
                    Elements eLatitudedegt = tabletext.select("[class=tabev1]");
                    Elements eLatitudedegtsyb = tabletext.select("[class=tabev2]");
                    Elements eLongitudedegt = tabletext.select("[class=tabev1]");
                    Elements eLongitudedegtsyb = tabletext.select("[class=tabev2]");
                    Elements eDeptht = tabletext.select("[class=tabev3]");
                    Elements eMagt = tabletext.select("[class=tabev2]");
                    Elements eRegiont = cells.select("[class=tb_region]");
                    for (Element time:eedatetimet ) {
                        Attributes a = time.attributes();
                        String     href = a.get("href").substring(30, 36);
                        String id=href;
                        String dateandtime=time.text();
                        String eeLatitude=eLatitudedegt.get(0).text().substring(0,eLatitudedegt.get(0).text().length()-2);
                        String Latitude=eeLatitude;
                        if(eLatitudedegtsyb.get(0).text().substring(0, 1).equals("N")){
                            //Latitude="+"+Latitude;
                        }else{
                            Latitude="-"+Latitude;
                        }
                        String Longitude=eLongitudedegt.get(1).text().substring(0,eLongitudedegt.get(0).text().length()-2);
                        if(eLongitudedegtsyb.get(0).text().substring(0, 1).equals("E")){
                            //Longitude="+"+Longitude;
                        }else{
                            Longitude="-"+Longitude;
                        }
                        String Depth=eDeptht.get(0).text();
                        String Mag=eMagt.get(2).text();
                        String Region=eRegiont.get(0).text();
                        while(Region.contains("'")){
                            Region= Region.substring(0,Region.indexOf("'", 0))+Region.substring(Region.indexOf("'", 0)+1,Region.length());
                            System.out.println(Region);
                        }
                        if(dateandtime.equals(stopDate)){
                            stop=true;
                        }
                        if(stop==true){
                            break;
                        }
                        if(!array.contains(id)){
                            stmt.executeUpdate("INSERT INTO quakes (id,UTC_date,latitude,longitude,depth,magnitude,region) VALUES"
                                    + "("+id+",'"+dateandtime+"',"+Latitude+","+Longitude+","+Depth+","+Mag+",'"+Region+"');");
                            conn.commit();
                            count ++;
                        }
                        array.add(id);
                    }
                }
                if(stop==true){
                    break;
                }
            }
            if(stop==true){
                break;
            }
        }
        return count;
    }
}
