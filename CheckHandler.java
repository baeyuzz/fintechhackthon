package Fintech;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.sql.*;


class CheckHandler implements HttpHandler {
    Connection conn;

    CheckHandler(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String message =null;
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String ID = br.readLine();
        boolean check = Register.CheckID(ID, conn);
        Headers ResponseHeader = exchange.getResponseHeaders();
        ResponseHeader.set("Content-Type", "text/plain");
        if (check)
           message="true\n";
        else
            message="false\n";
        String log = ID+ " : " +message;
        Logwriter.log_check(log);
        exchange.sendResponseHeaders(200, message.length());
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(message.getBytes());
        responseBody.flush();
        responseBody.close();
    }
}

class JoinHandler implements HttpHandler{
    Connection conn;

    JoinHandler(Connection conn){
        this.conn = conn;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("in join");
        String message = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        Gson gson = new Gson();
        String json = br.readLine();
        Person person = gson.fromJson(json, Person.class);
        boolean check = Register.RegisterUserData(person, conn);
        if (check)
            message = "true\n";
        else
            message = "false\n";
        String log = json +" : "+message;
        Logwriter.log_join(log);
        exchange.sendResponseHeaders(200, message.length());
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(message.getBytes());
        responseBody.close();
    }
}

class ClubinfoHandler implements HttpHandler{
    Connection conn;

    ClubinfoHandler(Connection conn){
        this.conn = conn;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("in clubinfo handler");

        try {


        String query, sub_query, captainID=null;
        ResultSet rs, sub_rs;
        PreparedStatement ps, sub_ps;
         BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String many = br.readLine();
        JsonObject json = new JsonObject();
        JsonParser jsonparser = new JsonParser();

        json = (JsonObject) jsonparser.parse(many);
        String id = json.get("id").toString().replace("\"","");
        String clubName = json.get("club_name").toString().replace("\"","");

        //id club name 받아옴
/*
String [] memberList,O
String captainID,
String [] meetingName,O
String[] meetingDate,O
int [] checkList(0이면 불참 1이면 참),O
int [] attendence
int[] meetingIsPaidO
        이 미팅이 지금 (결제요청중 (0), 결제요청전 (1), 결제완료 (2) )  3가지 상태는 나타내기"*/
            int clubNo, meetingNo;
            JsonArray nameArray = new JsonArray();
            JsonArray dateArray = new JsonArray();
            JsonArray checkArray = new JsonArray();
            JsonArray memberArray = new JsonArray();
            JsonArray numberArray = new JsonArray();
            JsonArray attendArray = new JsonArray();

            JsonArray paid = new JsonArray();

            Headers ResponseHeader = exchange.getResponseHeaders();
            ResponseHeader.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();

            query = "SELECT CLUB_ID FROM CLUB_MEMBER cb JOIN CLUB_LIST USING (CLUB_ID)" +
                "WHERE cb.USER_ID='" + id + "' AND club_name='" + clubName + "'";

            ps = conn.prepareStatement(query);
            rs = ps.executeQuery(query);
            rs.next();
            clubNo = rs.getInt(1);
            //모임 이름, 모임 날짜, 모임ID

            query = "SELECT MEETING_NAME, MEETING_DATE, MEETING_ID FROM MEETING_LIST " +
                    "WHERE CLUB_ID=" + clubNo;

            ps = conn.prepareStatement(query);
            rs = ps.executeQuery(query);
            while (rs.next()) {
                nameArray.add(rs.getString(1));
                dateArray.add(rs.getDate(2).toString());
                meetingNo = rs.getInt(3);
                //내가 이 모임에 참여 하는지. 0이면 불참 1이면 참

                sub_query = "SELECT COUNT(*) FROM MEETING_MEMBER WHERE MEETING_ID=" + meetingNo + " AND USER_ID='" + id + "'";
                sub_ps = conn.prepareStatement(sub_query);
                sub_rs = sub_ps.executeQuery(sub_query);
                sub_rs.next();
                checkArray.add(sub_rs.getInt(1));
                //모임 참여자 수

                sub_query = "SELECT COUNT(*) FROM MEETING_MEMBER WHERE MEETING_ID=" + meetingNo;
                sub_ps = conn.prepareStatement(sub_query);
                sub_rs = sub_ps.executeQuery(sub_query);
                sub_rs.next();
                numberArray.add(sub_rs.getInt(1));
            }
            //동아리 회원 목록

            query = "SELECT USER_NAME FROM CLUB_MEMBER JOIN USERS USING(USER_ID) WHERE CLUB_ID=" + clubNo;
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery(query);
            while (rs.next())
                memberArray.add(rs.getString(1));
            //로그인된 아이디의 가입된 동아리 목록과 동아리장 아이디 동아리 고유번호

            query = "SELECT cl.USER_ID FROM CLUB_MEMBER cb JOIN CLUB_LIST USING (CLUB_ID) JOIN CLUB_LEADER cl USING (CLUB_ID)" +
                    " WHERE cb.USER_ID='"+id+"'"+"and club_id="+clubNo;
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery(query);
            rs.next();
            captainID = rs.getString(1);

            for(int i = 0;i<nameArray.size();i++){

                query ="select pay_completed from pay_info join meeting_list using (meeting_id) where meeting_name='"+nameArray.get(i).getAsString()+"'";

                ps = conn.prepareStatement(query);
                rs = ps.executeQuery(query);
                rs.next();

                paid.add(rs.getInt(1));
            }

        JsonObject res = new JsonObject();
        res.add("memberList", memberArray);
        res.addProperty("captainID",captainID);
        res.add("meetingName", nameArray);
        res.add("meetingDate", dateArray);
        res.add("checkList",checkArray);
        res.add("attendence",numberArray);
        res.add("meetingIsPaid",paid);
            System.out.println(res);
/*

        String log = json+" : "+res;
        Logwriter.log_clubinfo(log);
        exchange.sendResponseHeaders(200,0);
        OutputStream responsebody = exchange.getResponseBody();
*/

            responseBody.write((res.toString()+"\n").getBytes());

            responseBody.flush();
            responseBody.close();

/*
 *

"동아리 회원 이름, 리더 아이디, 모임 목록(모임 이름, 모임 날짜, 내가 참석하는지 안하는지, 참여자 수)
 String [] memberList,
 String captainID,
  String [] meetingName,
   String[] meetingDate,
   int [] checkList(0이면 불참 1이면 참),
    int [] attendence
int[] meetingIsPaid
 이 미팅이 지금 (결제요청중 (0), 결제요청전 (1), 결제완료 (2) )  3가지 상태는 나타내기"
  * */
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
    }
}



class MakeroomHandler implements HttpHandler {
    Connection conn;

    MakeroomHandler(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("in make room");
        int check = -1;
        /*
        "{String club_name (동아리이름)
        String meeting_name (모임이름)
                String meeting_date (모임날짜)
        String int meeting_limit (모임한도) }"
        */
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));

            String many = br.readLine();

            JsonObject json = new JsonObject();
            JsonParser jsonpaser = new JsonParser();

            json = (JsonObject)jsonpaser.parse(many);
            String clubName = json.get("club_name").toString().replace("\"","");
            String meetingName = json.get("meeting_name").toString().replace("\"","");
            String meetingDate = json.get("meeting_date").toString().replace("\"","");
            String meetingLimit = json.get("meeting_limit").toString().replace("\"","");


            String query1 = "SELECT CLUB_ID FROM CLUB_LIST WHERE club_name='" + clubName+"'";

            PreparedStatement ps1 = conn.prepareStatement(query1);
            ResultSet rs1 = ps1.executeQuery(query1);
            int clubNo=0;
            while(rs1.next()) {
                clubNo = rs1.getInt(1);
            }
                String query2 = "INSERT INTO MEETING_LIST VALUES('" + clubNo + "',meeting_id.nextval,'" + meetingName+ "','" + meetingDate+ "'," + meetingLimit+")"; /*모임 만들기*/
                PreparedStatement ps2 = conn.prepareStatement(query2);
                check = ps2.executeUpdate(query2);
            System.out.println(query2);

            OutputStream responseBody = exchange.getResponseBody();
            if (check > 0) {
                String query3 = "insert into pay_info values(meeting_id.currval, null,null,0,null,1)";
                System.out.println(query3);

                PreparedStatement ps3 = conn.prepareStatement(query3);
                System.out.println("after prepare");

                int cnt = ps3.executeUpdate(query3);
                System.out.println("after cnt");

                if(cnt>0){
                    System.out.println("query3 success");
                }
                else
                    System.out.println("query3 fail");
                responseBody.write("true\n".getBytes());
            }
             else
                 responseBody.write("false\n".getBytes());
           // String log = json +" : " +message;
            responseBody.close();
         //   Logwriter.log_makeroom(log);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
class MeetinginfoHandler implements HttpHandler{
    Connection conn;
    public MeetinginfoHandler(Connection conn){
        this.conn = conn;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("in meeting info handler");

        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String meething_name = br.readLine();
        String query, meeting_date, log;
        int meeting_limit, meeting_num=0;
        JsonObject json = new JsonObject();
        JsonArray meeting_member = new JsonArray();
        PreparedStatement ps;
        ResultSet rs;
        try {
            query = "select cash_limit, meeting_date from meeting_list where meeting_name ='" + meething_name + "'";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery(query);
            rs.next();
            meeting_limit = rs.getInt(1);
            meeting_date = rs.getString(2);
            json.addProperty("meeting_limit", meeting_limit);
            json.addProperty("meeting_date", meeting_date);
            //모임에 참여하는 사람들 이름
            query = "select user_name from meeting_list join meeting_member using(meeting_id) join users using(user_id)" +
                    "where meeting_name='" + meething_name + "'";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery(query);
            while (rs.next()) {
                meeting_member.add(rs.getString(1));
                meeting_num++;
            }
            json.addProperty("meeting_num", meeting_num);
            json.add("meeting_memberList", meeting_member);
            exchange.sendResponseHeaders(200, (json.toString() + "\n").length());
            OutputStream responsBody = exchange.getResponseBody();
            responsBody.write((json.toString() + "\n").getBytes());
            log = meething_name+" : "+json.toString();
            Logwriter.log_meetinginfo(log);
            responsBody.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
/*
class MakeclubHandler implements HttpHandler{
    Connection conn;

    public MakeclubHandler(Connection conn){
        this.conn=conn;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int check = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String requestBody = br.readLine();
        Gson gson = new Gson();
        Makeclub_req makeclub_req= gson.fromJson(requestBody, Makeclub_req.class);
        //동아리 만들기
        String queay = "INSERT INTO CLUB_LIST VALUES("+club_id.nextval, 'Algorithm', SYSDATE, 'bank_account','bank_name');"

    }
}*/

