package android.ostfalia.teamandroid;

public class Contact {

    private String firstname;
    private String lastname;
    private String telephonenumber;
    private String street;
    private int housenumber;
    private long postcode;
    private String City;

    public Contact(String firstname, String lastname, String telephonenumber) {
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

    // The following attributes are nice to have:
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(int housenumber) {
        this.housenumber = housenumber;
    }

    public long getPostcode() {
        return postcode;
    }

    public void setPostcode(long postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }
}
