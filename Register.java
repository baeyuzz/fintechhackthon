package Fintech;
import oracle.jdbc.proxy.annotation.Pre;

import java.sql.*;

class Register {
	
    protected static boolean logincheck(String id, String pwC, Connection con) throws SQLException { 
    	// checking
		String pw;
        String query = "SELECT USER_PW FROM USERS WHERE USER_ID='" + id + "'";
        PreparedStatement pstm = con.prepareStatement(query);
        ResultSet rs = pstm.executeQuery(query);
        rs.next();
        pw = rs.getString(1);
        System.out.println(pw);
        if (pwC.equals(pw))
            return true;
        else
            return false;
    }

    protected static boolean RegisterUserData(Person person, Connection conn){
        String id, password, name, phone,birth;
        int cnt=-1;
        id = person.getID();
        password = person.getPassWord();
        name = person.getName();
        birth= person.getBirth();
        phone = person.getPhone();

        try{
            String query = "INSERT INTO USERS VALUES ('"+id+"', '"+password+"', '"+name+"', '"+birth+"', '"+phone+"'"+null+")";
            PreparedStatement ps = conn.prepareStatement(query);
            cnt = ps.executeUpdate(query);
        }catch(SQLException e){
            e.printStackTrace();
        }
        if(cnt >0)
            return true;
        else
            return false;
    }
    protected static boolean CheckID(String ID, Connection conn){
        String query = "SELECT COUNT(*) FROM USERS WHERE USER_ID='"+ID+"'";
        ResultSet rs = null;
        int cnt =1;
        try {
            PreparedStatement prstmt = conn.prepareStatement(query);
            rs = prstmt.executeQuery(query);
            rs.next();
            cnt = rs.getInt(1);
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        if(cnt == 0)
            return true;
        else
            return false;
    }
}
