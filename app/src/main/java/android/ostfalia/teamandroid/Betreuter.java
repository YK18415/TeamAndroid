package android.ostfalia.teamandroid;

public class Betreuter {

    private String firstname;
    private String lastname;
    private String telephonenumber;

    public Betreuter(String firstname, String lastname, String telephonenumber) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.telephonenumber = telephonenumber;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getTelephonenumber() {
        return telephonenumber;
    }
}
