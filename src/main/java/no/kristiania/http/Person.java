package no.kristiania.http;

public class Person {

    private String firstName;
    private String lastName;

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return "first name :" + firstName + ". Last name: "+ lastName + '\n';
    }
}
