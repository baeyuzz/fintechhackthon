package Fintech;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

class Logwriter{

    static public void log_check(String string) throws  IOException{
        BufferedWriter checklog = new BufferedWriter(new FileWriter("doublecheck_log.txt",true));
        checklog.write(string); checklog.newLine();
        checklog.close();
    }

    static public void log_clubinfo(String string) throws IOException{
        BufferedWriter clubinfolog = new BufferedWriter(new FileWriter("clubinfo_log.txt", true));
        clubinfolog.write(string); clubinfolog.newLine();
        clubinfolog.close();
    }

    static public void log_join(String string) throws IOException{
        BufferedWriter joinglog = new BufferedWriter(new FileWriter("join_log.txt", true));
        joinglog.write(string); joinglog.newLine();
        joinglog.close();
    }

    static public void log_makeroom(String string) throws IOException{
        BufferedWriter makeroomlog = new BufferedWriter(new FileWriter("makeroom_log.txt",true));
        makeroomlog.write(string); makeroomlog.newLine();
        makeroomlog.close();
    }

    static public void log_invite(String string) throws IOException{
        BufferedWriter invitelog = new BufferedWriter(new FileWriter("invite_log.txt",true));
        invitelog.write(string); invitelog.newLine();
        invitelog.close();
    }

    static public void log_login(String string) throws  IOException{
        BufferedWriter loginlog = new BufferedWriter(new FileWriter("login_log.txt", true));
        loginlog.write(string); loginlog.newLine();
        loginlog.close();
    }

    static public void log_search(String string) throws IOException{
        BufferedWriter searchlog = new BufferedWriter(new FileWriter("search_log.txt", true));
        searchlog.write(string); searchlog.newLine();
        searchlog.close();
    }

    static public void log_meetinginfo(String string) throws IOException{
        BufferedWriter log = new BufferedWriter(new FileWriter("meetinginfo_log.txt", true));
        log.write(string); log.newLine();
        log.close();
    }

    static public void log_clubacc(String string) throws IOException{
        BufferedWriter log = new BufferedWriter(new FileWriter("clubAcc_log.txt", true));
        log.write(string); log.newLine();
        log.close();
    }
}