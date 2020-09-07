package Fintech;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;



public class Main {
    static protected Connection conn = null;

    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@mydb.c3zh7uuwurgd.ap-northeast-2.rds.amazonaws.com:1521:orcl";
        String user = "123";
        String pass = "123";
        try {
            InetSocketAddress addr = new InetSocketAddress(8080);
            HttpServer server = HttpServer.create(addr, 0);
            System.out.println(addr.toString());
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("DB connected");
            server.createContext("/doublecheck", new CheckHandler(conn));
            server.createContext("/join", new JoinHandler(conn));
            server.createContext("/getclubinfo", new ClubinfoHandler(conn));
            server.createContext("/makeroom",new MakeroomHandler(conn));
            server.createContext("/login", new LoginHandler(conn));
            server.createContext("/finduser", new SearchHandler(conn));
            server.createContext("/inviteuser", new InviteHandler(conn));
            server.createContext("/getmeetinginfo",new MeetinginfoHandler(conn));
            server.createContext("/getClubAcc",new GetAccListHandler(conn));
            server.createContext("/InPayInfo", new PayInfoHandler(conn));
            server.createContext("/getBill", new ShowReceipt(conn));
            server.createContext("/getAuthRequest", new GetAuthRequestHandler(conn));
            server.createContext("/sendAuthRequest", new SendAuthRequestHandler(conn));
            server.createContext("/setClubFee", new SetClubFeeHandler(conn));
            server.createContext("/registerAccount", new RegisterAccountHandler(conn));
            server.createContext("/changeClub", new ChangeClubHandler(conn));

            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            System.out.println("class not found");
        } catch (SQLException sqle) {
            System.out.println("sql error");
        }
    }

    void Jsontest() {
        DataInputStream is;
        String json = "{\"ID\":\"gwangho0510\",\"Password\":\"mason986511\"," +
                "\"Name\":\"Gwangho Lee\",\"birth\":\"19940510\",\"phone\": \"01066046059\"}";
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                is = new DataInputStream(socket.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                Gson gson = new Gson();
                Person person = gson.fromJson(br.readLine(), Person.class);
                System.out.println("ID : " + person.getID());
                System.out.println("Password : " + person.getPassWord());
                System.out.println(person.getName());
                System.out.println(person.getBirth());
                System.out.println(person.getPhone());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

