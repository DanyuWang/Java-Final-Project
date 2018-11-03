import java.sql.*;

/**
 * @author Shen Yichao
 */
public class SqlCon {

    private String sqlPos;
    Connection con = null;

    /**
     *
     * @param name represent the name of the database that we want to connect to
     */
    public SqlCon(String name) {
        sqlPos = name;
    }

    /**
     * connect to the database<br>
     * if failed,exit the program<br>
     * @throws SQLException in case exception
     */
    public void connectDB() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.err.println("Cannot find the driver.");
            System.exit(1);
        }
        con = DriverManager.getConnection("jdbc:sqlite:" + sqlPos);
        con.setAutoCommit(false);
    }

    /**
     * @return  return all the information in the table
     * @throws SQLException in case exception
     */
    public ResultSet readFromDB() throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        stmt = con.prepareStatement(
                "select id,substr(UTC_date, 1, 19), latitude,longitude,depth,magnitude,region,area_id from quakes" );
        rs = stmt.executeQuery();
        return rs;
    }


}
