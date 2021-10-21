package no.kristiania.person;

import no.kristiania.person.Person;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PersonDaoTest {
    private PersonDao dao = new PersonDao(TestData.testDataSource());

    @Test
    void shouldRetrieveSavedPerson() throws SQLException {
        Person person = examplePerson();
        dao.save(person);

        assertThat(dao.retrieve(person.getId()))
                .hasNoNullFieldsOrProperties()// ikke mangler noen properties
                .usingRecursiveComparison()
                .isEqualTo(person)
                ;
    }

    private Person examplePerson() {
        Person person = new Person();
        person.setFirstName(TestData.pickOne("Sigmund","Sandra","Jacob","Kåre"));
        person.setLastName(TestData.pickOne("Olsen","Steinsberg","Petterson","Hagen"));
        return person;

    }
}
