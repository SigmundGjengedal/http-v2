package no.kristiania.person;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class PersonDao {
    private final DataSource dataSource;

    public PersonDao(DataSource testDataSource) {
        this.dataSource = testDataSource;
    }

    public void save(Person person) throws SQLException {
        try (Connection connection = dataSource.getConnection() ) {
            try (PreparedStatement statement = connection.prepareStatement("insert into people (first_name, last_name) values (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1,person.getFirstName());
                statement.setString(2,person.getLastName());

                statement.executeUpdate();

                try (ResultSet rsKeys = statement.getGeneratedKeys()) {
                    rsKeys.next();
                    person.setId(rsKeys.getLong("id"));
                }
            }
        }
    }

    public Person retrieve(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from people where id = ?")) {
                statement.setLong(1,id);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    Person person = new Person();
                    person.setId(rs.getLong("id")); // setter id og navn i objektet
                    person.setFirstName(rs.getString("first_name"));
                    person.setLastName(rs.getString("last_name"));
                    return person;
                }
            }
        }
    }

    public List<Person> listAll() {
        return null;
    }
}
