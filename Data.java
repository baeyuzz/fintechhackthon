package Fintech;

class Makeclub_req{
    private String Club_name, Club_leader, Accont_bank, Account_num, leader_birth;

    protected String getClub_name(){
        return this.Club_name;
    }

    protected String getClub_leader(){
        return this.Club_leader;
    }

    protected String getAccont_bank(){
        return this.Accont_bank;
    }

    protected String getAccount_num(){
        return this.Account_num;
    }

    protected String getLeader_birth(){
        return this.leader_birth;
    }
}
class Makeroom_req{
    private String club_name;
    private String meeting_name;
    private String date;
    private int budget;

    public String getMeeting_name(){
        return this.meeting_name;
    }

    public String getClub_name() {
        return club_name;
    }


    public String getDate() {
        return date;
    }

    public int getBudget() {
        return budget;
    }
}

class Clubinfo_req{
    private String id;
    private String club_name;

    public String getId(){
        return this.id;
    }

    public String getClub_name(){
        return this.club_name;
    }
}

class Person {
    private String id;
    private String pw;
    private String name;
    private String birth;
    private String phone;

    public String getID() {
        return this.id;
    }

    public String getPassWord() {
        return this.pw;
    }

    public String getName() {
        return this.name;
    }

    public String getBirth() {
        return this.birth;
    }

    public String getPhone() {
        return this.phone;
    }
}
