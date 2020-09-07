package server;

import java.sql.*;

public class DBConnection {
    private static Connection con;

    public static Connection getConnection(){
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection("jdbc.oracle:thin:@mydb.c3zh7uuwurgd.ap-northeast-2.rds.amazonaws.com:1521:orcl", "123", "123");

            System.out.println("DB Connected");



        } catch (ClassNotFoundException cnfe) {
            System.out.println("DB Driver Loading Failure : " + cnfe.toString());
        } catch (SQLException sqle) {
            System.out.println("DB Connection Failure : " + sqle.toString());
        } catch (Exception e) {
            System.out.println("Unknown Error");
            e.printStackTrace();
        }
        return con;
    }
}




/*
String json = "{\"ID\":\"gwangho0510\",\"Password\":\"mason986511\"," +
                "\"name\":\"Gwangho Lee\",\"birth\":\"19940510\",\"phone\": \"01066046059\"}";
 */