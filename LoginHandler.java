package Fintech;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import oracle.jdbc.proxy.annotation.Pre;

import javax.swing.plaf.nimbus.State;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.Buffer;
import java.sql.*;
class LoginHandler implements HttpHandler {
    Connection con;

    public LoginHandler(Connection con) {
        this.con = con;
    }

//성공하면 동아리 이름, 회장, 인원수, 개인정보
// 실패하면 false
// String clubName, clubLeader, clubNo, id,pw,name,birth,phone

    public void handle(HttpExchange exchange) throws IOException {
        int memberno;
        // 로그인 후
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String logininfo = br.readLine();
            JsonObject json = new JsonObject();
            JsonParser jsonparser = new JsonParser();
            JsonArray Club_name = new JsonArray();
            JsonArray Club_leader = new JsonArray();
            JsonArray Club_No = new JsonArray();

            JsonObject idpw = (JsonObject) jsonparser.parse(logininfo);// string 으로 받은 login info
            String id = idpw.get("id").toString().replace("\"", "");
            String pw = idpw.get("pw").toString().replace("\"", "");
            System.out.println(id + pw);

            boolean check = Fucntion.logincheck(id, pw, con); //id, pw 보내서 체크함
            System.out.println(check);

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            if (check) {
                String query1 = "SELECT CLUB_NAME, cl.USER_ID, CLUB_ID FROM CLUB_MEMBER cb JOIN CLUB_LIST USING (CLUB_ID) JOIN CLUB_LEADER cl USING (CLUB_ID)" +
                        "WHERE cb.USER_ID='" + id + "'"; // 저 아이디 통해서 동아리이름, 동아리회장, 동아리고유 번호 받아옴
                String query2 = null;
                String query3 = "SELECT USER_NAME, USER_BIRTH, USER_PHONE FROM USERS WHERE USER_ID='" + id + "'";
                String query4 = "select bank_account from users where user_id='"+id+"'";

                PreparedStatement ps1 = con.prepareStatement(query1);
                ResultSet rs1 = ps1.executeQuery(query1);

                PreparedStatement ps3 = con.prepareStatement(query3);
                ResultSet rs3 = ps3.executeQuery(query3);
                rs3.next();
                String name = rs3.getString(1);
                String birth = rs3.getString(2);
                String phone = rs3.getString(3);

                PreparedStatement ps4 = con.prepareStatement(query4);
                ResultSet rs4 = ps4.executeQuery(query4);
                rs4.next();
                String account = rs4.getString(1);

                while (rs1.next()) {
                    String clubname = rs1.getString(1);
                    String clubleader = rs1.getString(2);
                    String clubno = rs1.getString(3); // club no 클럽의 고유 번호 얘는 json 관련x

                    query2 = "SELECT count(*) FROM CLUB_LIST JOIN CLUB_MEMBER USING(CLUB_ID) WHERE CLUB_ID=" + clubno;
                    PreparedStatement ps2 = con.prepareStatement(query2);
                    ResultSet rs2 = ps2.executeQuery(query2);
                    rs2.next();
                    memberno = rs2.getInt(1); // 동아리 인원 수

                    Club_name.add(clubname);
                    Club_leader.add(clubleader);
                    Club_No.add(memberno);
                }

                json.addProperty("id", id);
                json.addProperty("pw", Fucntion.pw); // 패스워드도 보내야하나? 굳이????
                json.addProperty("name", name);
                json.addProperty("birth", birth);
                json.addProperty("phone", phone);
                json.add("clubName", Club_name);
                json.add("clubLeader", Club_leader);
                json.add("clubNo", Club_No);
                json.addProperty("myAccNumber",account);

                System.out.println(json.toString());


                responseBody.write((json.toString() + "\n").getBytes());
                responseBody.close();

            } else
                responseBody.write("false\n".getBytes());
            responseBody.flush();
            responseBody.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}




class SearchHandler implements HttpHandler {
    Connection con;

    // 성공하면 회원 이름 name 과 id 실패시 false?

    public SearchHandler(Connection con) {
        this.con = con;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {        System.out.println("in search handler");

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String id = br.readLine();
            String query = "SELECT USER_NAME FROM USERS WHERE USER_ID='" + id + "'";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery(query);
            String message=null;
            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain"); // 여기를 어떻게 함??????
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();
            String name = null;
            while(rs.next()) {
                name = rs.getString(1);
                message = name+"\n";
            }
            if(name == null)
                message = "false\n";
            responseBody.write(message.getBytes());
            String log = id +" : "+message;
            Logwriter.log_search(log);
            responseBody.flush();
            responseBody.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


class InviteHandler implements HttpHandler {
    // club name, id 옴..
    // 성공하면 true 실패하면 false
    // request club_name, user_id
    Connection con;

    public InviteHandler(Connection con) {
        this.con = con;
    }


    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("in invite");

        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String idclubinfo = br.readLine();
        JsonParser jsonparser = new JsonParser();
        JsonObject idclub = (JsonObject) jsonparser.parse(idclubinfo); // string 으로 받은 login info
        String club_name = idclub.get("club_name").toString().replace("\"","");
        String id = idclub.get("user_id").toString().replace("\"","");
        int cnt = -1;

        Headers ResponseHeader = exchange.getResponseHeaders();
        ResponseHeader.set("Content-Type", "text/plain"); // 여기를 어떻게 함??????
        exchange.sendResponseHeaders(200, 0);
        OutputStream responseBody = exchange.getResponseBody();
        String message;
        try {
            String query1 = "SELECT distinct CLUB_ID FROM CLUB_MEMBER cb JOIN CLUB_LIST USING (CLUB_ID) join club_leader using(club_id)"+
                    "WHERE club_name='"+club_name+"'";
            // "INSERT INTO 클럽목록 VALUES ('"+id+"')";
            PreparedStatement st1 = con.prepareStatement(query1);
            ResultSet rs1 = st1.executeQuery(query1);
            rs1.next();
            int Club_id = rs1.getInt(1);
            String query2 = "INSERT INTO CLUB_MEMBER VALUES ('" + id + "', '" + Club_id + "')"; /*동아리 가입*/
            PreparedStatement st2 = con.prepareStatement(query2);
            cnt = st2.executeUpdate(query2);
            if (cnt > 0)
                message = "true\n";

            else
                message = "false\n";
            responseBody.write(message.getBytes());
            responseBody.flush();
            responseBody.close();
            String log = idclubinfo+" : "+ message;
            Logwriter.log_invite(log);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class GetAccListHandler implements HttpHandler {
    Connection conn;

    public GetAccListHandler(Connection conn) {
        this.conn = conn;
    }

    /*
    String clube name 이 오고
    return 값
    {String[] AccDate;  (거래날짜)
    String[] AccPayMan; (결제자)
    String[] AccName; (모임이름)
    int[] AccNum;  모임인원)
    String[] AccPlace; (거래 장소)
    int[] AccPrice; (거래금액)
    */
    public void handle(HttpExchange exchange) throws IOException {

        try {
            System.out.println("in getAcc");

            JsonArray accDate = new JsonArray();
            JsonArray accPayman = new JsonArray();
            JsonArray accName = new JsonArray();
            JsonArray accNum = new JsonArray();
            JsonArray accPlace = new JsonArray();
            JsonArray accPrice = new JsonArray();

            JsonObject json = new JsonObject();

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String clubname = br.readLine().replace("\"", "");

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain"); // 여기를 어떻게 함??????
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

sd
            /*거래내역에서 날짜, 모임 이름, 결제한 사람, 장소, 가격*/
            String query = "select meeting_date, meeting_name, USER_ID, place, price from pay_info join meeting_list using(meeting_id) " +
                    "join club_list using (club_id) where club_name='" + clubname + "'"; // db에서 받아오기


            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery(query);

            PreparedStatement ps2;
            ResultSet rs2;

            System.out.println(query);

            while (rs.next()) {

                accDate.add(rs.getString(1));
                accPayman.add(rs.getString(3));
                accName.add(rs.getString(2));
                accPlace.add(rs.getString(4));
                accPrice.add(rs.getInt(5));

            }

            int cnt = accName.size();
            for (int i = 0; i < cnt; i++) {


                String query2 = "SELECT count(*) FROM MEETING_list join meeting_member using (meeting_id)" +
                        "WHERE MEETING_NAME='" + accName.get(i).getAsString() + "'";

                ps2 = conn.prepareStatement(query2);
                rs2 = ps2.executeQuery(query2);
                System.out.println(query2);

                rs2.next();
                int tmp = rs2.getInt(1);

                accNum.add(tmp);
            }

            json.add("AccDate", accDate);
            json.add("AccPayMan", accPayman);
            json.add("AccName", accName);
            json.add("AccNum", accNum);
            json.add("AccPlace", accPlace);
            json.add("AccPrice", accPrice);
            System.out.println(json);

            responseBody.write((json.toString() + "\n").getBytes());
            responseBody.close();


            responseBody.flush();
            responseBody.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class ChangeClubHandler implements HttpHandler {
    Connection conn;
    public ChangeClubHandler(Connection conn){
        this.conn=conn;
    }
    /*
    String clube name 이 오고
    return 값
    {String[] AccDate;  (거래날짜)
    String[] AccPayMan; (결제자)
    String[] AccName; (모임이름)
    int[] AccNum;  모임인원)
    String[] AccPlace; (거래 장소)
    int[] AccPrice; (거래금액)
    */
    public void handle(HttpExchange exchange) throws IOException {

        try {
            System.out.println("in change");

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String idname = br.readLine();
            JsonParser jsonparser = new JsonParser();
            JsonObject json = new JsonObject();

            json = (JsonObject) jsonparser.parse(idname);

            String id = json.get("id").toString().replace("\"","");
            String clubName = json.get("club_name").toString().replace("\"","");

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            int ClubNum = 0; // (동아리인원수);
            String ClubDate=null; // (동아리만든날짜);
            String ClubLeaderId = null; // (동아리장 아이디) ;
            String ClubAcc=null; // (동아리계좌번호);
            int ClubFee=0;// (동아리 회비);"
            int clubid = 0;

            
            int ClubMoney=0; // (동아리 남은회비통장잔액);
            int[] ClubMemberMoney; // (멤버별 각각 낸 회비금액);
            JsonArray ClubPayedMember = new JsonArray();// (동아리 멤버 리스트);

            JsonArray memberList = new JsonArray();

            /*
            JsonArray meetingName = new JsonArray();
            JsonArray meetingDate = new JsonArray();
            JsonArray meetingid = new JsonArray();
            JsonArray meetingIsPayed = new JsonArray();
            JsonArray checkList = new JsonArray(); //(0이면 불참 1이면 참)
            JsonArray attendence = new JsonArray();

*/
            /*내가 속한 동아리 리더 아이디, 만든날짜, 계좌정보, 회비*/
            String query1 = "SELECT cl.USER_ID, club_made_day, bank_account, membership_fee, club_id" +
                    " FROM CLUB_MEMBER cb JOIN CLUB_LIST USING (CLUB_ID) JOIN CLUB_LEADER cl USING (CLUB_ID)" +
                    " WHERE cb.USER_ID='"+id+"' and club_name='"+clubName+"'";

            System.out.println(id+" "+clubName);

            PreparedStatement ps1 = conn.prepareStatement(query1);
            ResultSet rs1 = ps1.executeQuery(query1);

            JsonObject res = new JsonObject();
            rs1.next();
            System.out.println("in");
              ClubLeaderId = rs1.getString(1).replace("\"","");
              ClubDate = rs1.getString(2).replace("\"","");
              ClubAcc = rs1.getString(3).replace("\"","");
              ClubFee = rs1.getInt(4);
              clubid = rs1.getInt(5);

              res.addProperty("ClubLeaderId",ClubLeaderId);
            res.addProperty("ClubDate",ClubDate);
            res.addProperty("ClubAcc",ClubAcc);
            res.addProperty("ClubFee",ClubFee);


            /*동아리 회원 목록, 인원수*/
            String query2 = "select user_id from club_member where club_id="+clubid;
            PreparedStatement ps2 = conn.prepareStatement(query2);
            ResultSet rs2 = ps2.executeQuery(query2);
            while(rs2.next()){
                System.out.println("in2");
                memberList.add(rs2.getString(1));
            }
            for(int i = 0;i<memberList.size();i++){
                System.out.println("in3");

                ClubNum++;
            }
            res.addProperty("ClubNum",ClubNum);

            System.out.println(res);
            responseBody.write((res.toString()+"\n").getBytes());

            responseBody.flush();
            responseBody.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
/*

class PayInfoHandler implements HttpHandler {

    Connection con;

    public PayInfoHandler(Connection con) {
        this.con = con;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
              */
/*
    request
            private static int PayPrice; (결제금액)
            private static String PayPlace; (결제장소)
            private static byte[] BillByte; (영수증이미지)
            String meeting_name (모임이름)
            String id (결제자 아이디)
            String name (결제자이름)
            String club_name (동아리 이름)

     response true / false
    *//*

            System.out.println("server in");
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String many = br.readLine();
            JsonObject json = new JsonObject();
            JsonParser jsonparser = new JsonParser();
            JsonObject PayPrice = new JsonObject();
            JsonObject PayPlace = new JsonObject();
            JsonObject BillByte = new JsonObject();
            JsonObject meeting_name = new JsonObject();
            JsonObject name = new JsonObject();
            JsonObject club_name = new JsonObject();

            json = (JsonObject) jsonparser.parse(many);
            String id = json.get("id").toString().replace("\"", "");
            String payPlace = json.get("PayPlace").toString().replace("\"", "");
            int payPrice = json.get("PayPrice").getAsInt();
            byte[] billByte = json.get("BillByte").toString().getBytes();
            String meetingName = json.get("meeting_name").toString().replace("\"", "");

            System.out.println(json);

            int meetingId = 0;

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain"); // 여기를 어떻게 함??????
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            String query1 = "select meeting_id from meeting_list where meeting_name='"+meetingName+"'";
            Statement st=con.createStatement();
            System.out.println(query1);
            //PreparedStatement st = con.prepareStatement(query1);
            ResultSet rs1 = st.executeQuery(query1);


            while(rs1.next()) {
                meetingId = rs1.getInt(1);
                System.out.println(meetingId);
            }
            int cnt = -1;

            String query2 = "insert into pay_info values(?,?,?,?,?,?)";

            PreparedStatement st2 = con.prepareStatement(query2);
            st2.setInt(1,meetingId);
            st2.setString(2,id);
            st2.setString(3,payPlace);
            st2.setInt(4,payPrice);
            st2.setBytes(5,billByte);
            st2.setString(6,"0");


            cnt = st2.executeUpdate();

            if (cnt > 0)
                responseBody.write("true\n".getBytes());

            else
                responseBody.write("false\n".getBytes());

            responseBody.flush();
            responseBody.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
*/


class PayInfoHandler implements HttpHandler {

    Connection con;

    public PayInfoHandler(Connection con) {
        this.con = con;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            System.out.println("in pay info");

              /*
    request
            private static int PayPrice; (결제금액)
            private static String PayPlace; (결제장소)
            private static byte[] BillByte; (영수증이미지)
            String meeting_name (모임이름)
            String id (결제자 아이디)
            String name (결제자이름)
            String club_name (동아리 이름)

     response true / false
    */
            System.out.println("server in");
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String many = br.readLine();
            JsonObject json = new JsonObject();
            JsonParser jsonparser = new JsonParser();
            JsonObject PayPrice = new JsonObject();
            JsonObject PayPlace = new JsonObject();
            JsonObject BillByte = new JsonObject();
            JsonObject meeting_name = new JsonObject();
            JsonObject name = new JsonObject();
            JsonObject club_name = new JsonObject();

            json = (JsonObject) jsonparser.parse(many);
            String id = json.get("id").toString().replace("\"", "");
            String payPlace = json.get("PayPlace").toString().replace("\"", "");
            int payPrice = json.get("PayPrice").getAsInt();
            byte[] billByte = json.get("BillByte").toString().getBytes();
            String meetingName = json.get("meeting_name").toString().replace("\"", "");

            System.out.println(json);

            int meetingId = 0;

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain"); // 여기를 어떻게 함??????
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            String query1 = "select meeting_id from meeting_list where meeting_name='"+meetingName+"'";
            Statement st=con.createStatement();
            System.out.println(query1);
            ResultSet rs1 = st.executeQuery(query1);

            while(rs1.next()) {
                meetingId = rs1.getInt(1);
                System.out.println(meetingId);
            }
            int cnt = -1;
            System.out.println("before query 2");

            String query2 = "update pay_info set user_id=?,place=?,price=?,receipt=?, pay_completed=? where meeting_id="+meetingId;

            PreparedStatement st2 = con.prepareStatement(query2);

            st2.setString(1,id);
            st2.setString(2,payPlace);
            st2.setInt(3,payPrice);
            st2.setBytes(4,billByte);
            st2.setInt(5,0);
            //  st2.setString(6,"0");

            cnt = st2.executeUpdate();
            if (cnt > 0){
                responseBody.write("true\n".getBytes());

                JsonArray userId = new JsonArray();

                String query3 = "select user_id from meeting_member where meeting_id="+meetingId;
                PreparedStatement ps = con.prepareStatement(query3);
                ResultSet rs = ps.executeQuery(query3);
                while(rs.next()){
                    userId.add(rs.getString(1));
                }

                for(int i=0;i<userId.size();i++){
                    String query4 = "insert into pay_request values ("+meetingId+",'"+userId.get(i).toString()+"','0')";
                    Statement st1 = con.createStatement();
                    st1.executeUpdate(query4);
                }

            }

            else
                responseBody.write("false\n".getBytes());

            responseBody.flush();
            responseBody.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}


class ShowReceipt implements HttpHandler {
    Connection conn;

    public ShowReceipt(Connection conn) {
        this.conn = conn;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            System.out.println("in receipt handler");

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String meeting_name = br.readLine().replace("\"", "");

            /*거래내역에서 날짜, 모임 이름, 결제한 사람, 장소, 가격*/

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            JsonObject json = new JsonObject();
            String billByte = null;

            String query = "select receipt from pay_info join meeting_list using(meeting_id) where meeting_name='" + meeting_name + "'";
            System.out.println(query);
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery(query);

            while (rs.next()) {
                billByte = json.get("BillByte").toString();
            }
            System.out.println(json);
            responseBody.write((billByte + "\n").getBytes());
            responseBody.close();

            responseBody.flush();
            responseBody.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


class GetAuthRequestHandler implements HttpHandler {
    Connection con;
    public GetAuthRequestHandler (Connection con){
        this.con = con;
    }

    public void handle(HttpExchange exchange) throws IOException{

        try{
            JsonArray clubName = new JsonArray();
            JsonArray meetingName = new JsonArray();
            JsonArray price = new JsonArray();
            JsonArray requester = new JsonArray();
            JsonArray place = new JsonArray();
            JsonObject json = new JsonObject();

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String id = br.readLine().replace("\"","");

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            int meetingId =0;

            String query = "select meeting_id from pay_request where user_id='"+id+"' and agreement=0";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery(query);

            System.out.println(query);

            while(rs.next()) {
                meetingId = rs.getInt(1);


                String query2 = "select club_name, meeting_name, price, user_id, place from club_list " +
                        "join meeting_list using(club_id) join pay_info using(meeting_id) where meeting_id=" + meetingId;

                System.out.println(query2);

                PreparedStatement ps2 = con.prepareStatement(query2);
                ResultSet rs2 = ps2.executeQuery(query2);


 /*
            request String id
            "성공시
            String []  club_name,
            String [] meeting_name,
            int [] amount,
            String [] request_man,
            String [] place
            실패시 false"
            */


                while (rs2.next()) {
                    clubName.add(rs2.getString("club_name"));
                    meetingName.add(rs2.getString("meeting_name"));
                    price.add(rs2.getInt("price"));
                    requester.add(rs2.getString("user_id"));
                    place.add(rs2.getString("place"));
                }
            }

            json.add("club_name", clubName);
            json.add("meeting_name", meetingName);
            json.add("amount", price);
            json.add("request_man", requester);
            json.add("place", place);

            System.out.println(json);

            responseBody.write((json.toString()+ "\n").getBytes());


            responseBody.flush();
            responseBody.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}


class SendAuthRequestHandler implements HttpHandler {
    Connection con;

    public SendAuthRequestHandler (Connection con) {this.con = con;}

    public void handle(HttpExchange exchange) throws  IOException {

        try{

            System.out.println("Send Auth Request");
//            String id, String club_name, String meeting_name, int auth (-1이면 거절, 1이면 승인)
            //
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String many = br.readLine();
            JsonObject json = new JsonObject();
            JsonParser jsonparser = new JsonParser();

            json = (JsonObject) jsonparser.parse(many);
            String id = json.get("id").toString().replace("\"", "");
          /*  String clubName = json.get("club_name").toString().replace("\"", "");
            String meetingName = json.get("meeting_name").toString().replace("\"", "");
           */
            int auth  = json.get("auth").getAsInt();


            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            int meetingId =0;

            String query = "select meeting_id from pay_request where user_id='"+id+"' and agreement = 0";
            System.out.println("query1 : "+query);

            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery(query);

            while(rs.next())
                meetingId = rs.getInt(1);

            int cnt =-1;

            String query2 = "update pay_request set agreement="+auth+" where user_id='"+id+"' and meeting_id ="+meetingId;
            Statement st = con.createStatement();
            cnt = st.executeUpdate(query2);
            System.out.println("query2 : "+query2);

            if(cnt>0) {
                responseBody.write("true\n".getBytes());
                System.out.println("true");
            }
            else {
                responseBody.write("false\n".getBytes());
                System.out.println("false");

            }
            responseBody.flush();
            responseBody.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


class RegisterAccountHandler implements HttpHandler {
    Connection con;
    public RegisterAccountHandler (Connection con){this.con = con;}
    @Override
    public void handle(HttpExchange exchange) throws IOException{
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String idacc = br.readLine();

            JsonParser jsonparser = new JsonParser();
            JsonObject json = new JsonObject();

            json = (JsonObject) jsonparser.parse(idacc);
            String id = json.get("id").toString().replace("\"", "");
            String acc = json.get("account_num").toString().replace("\"","");

            System.out.println(id);
            System.out.println(acc);

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            int cnt = -1;
            String query = "update users set BANK_ACCOUNT='"+acc+"' where user_id='"+id+"'";
            System.out.println(query);
            Statement st = con.createStatement();
            cnt = st.executeUpdate(query);

            if(cnt>0)
                responseBody.write("true\n".getBytes());

            else
                responseBody.write("false\n".getBytes());

            responseBody.flush();
            responseBody.close();


       /*
        String id, String account_num	성공시 true, 실패시 false
*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


class SetClubFeeHandler implements HttpHandler {
    Connection con;
    public SetClubFeeHandler (Connection con) {this.con = con;}
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String nameFee = br.readLine();

            JsonParser jsonparser = new JsonParser();
            JsonObject json = new JsonObject();
            json = (JsonObject) jsonparser.parse(nameFee);

            String clubName = json.get("club_name").toString().replace("\"", "");
            int clubFee = json.get("ClubFee").getAsInt();


            int cnt = -1;

            System.out.println(clubFee);
            System.out.println(clubName);

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();


            String query = "update club_list set MEMBERSHIP_FEE=" + clubFee + " where club_name='" + clubName + "'";
            System.out.println(query);
            PreparedStatement ps = con.prepareStatement(query);
            cnt = ps.executeUpdate(query);

            System.out.println(cnt);
            if (cnt > 0)
                responseBody.write("true\n".getBytes());

            else
                responseBody.write("false\n".getBytes());

            responseBody.flush();
            responseBody.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

/*
        String club_name(동아리이름), int ClubFee (회비금액)
        true, false
*/
    }
}

class FiftyHandler implements HttpHandler {
    Connection con;
    public FiftyHandler (Connection con) {this.con =con;}
    @Override
    public void handle(HttpExchange exchange) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String nameFee = br.readLine();

    }
}