package android.ostfalia.teamandroid;

import java.io.Serializable;

public class Contact implements Serializable {

    private String firstname;
    private String lastname;
    private String telephonenumber;
    private String street;
    private String housenumber;
    private String postcode;
    private String City;

    /**
     * Creates a new object with some information
     * @param firstname The first name of the new contact
     * @param lastname The last name of the new contact
     * @param telephonenumber The phone number of the new contact
     */
    public Contact(String firstname, String lastname, String telephonenumber) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.telephonenumber = telephonenumber;
    }

    /**
     * Gets the first name
     * @return First name
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Gets the last name
     * @return Last name
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Gets the phone number
     * @return The phone number
     */
    public String getTelephonenumber() {
        return telephonenumber;
    }

    // The following attributes are nice to have:

    /**
     * Gets the street name
     * @return The street name
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street name
     * @param street The new street name
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets the house number
     * @return The house number
     */
    public String getHousenumber() {
        return housenumber;
    }

    /**
     * Sets the house number
     * @param housenumber The house number
     */
    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    /**
     * Gets the postal code
     * @return The postal code
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postal code
     * @param postcode The postal code
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Gets the city name
     * @return The city name
     */
    public String getCity() {
        return City;
    }

    /**
     * Sets the city name
     * @param city The city name
     */
    public void setCity(String city) {
        City = city;
    }
}
