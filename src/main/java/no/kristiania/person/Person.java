package no.kristiania.person;

public class Person {

    private String firstName;
    private String lastName;
    private long id;

    public String getLastName() {
        return lastName;
    }

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
