package no.kristiania.http;

import no.kristiania.person.PersonDao;
import no.kristiania.person.RoleDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PersonnelServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) throws IOException {
        DataSource dataSource = createDataSource();
        RoleDao roleDao = new RoleDao(dataSource);
        PersonDao personDao = new PersonDao(dataSource);

        HttpServer httpServer = new HttpServer(1800);
        // hvor vi adder data til databasen. En controller gis en tilsvarende dao:
        httpServer.addController("/api/roleOptions", new RoleOptionsController(roleDao));
        httpServer.addController("/api/newPerson", new AddPersonController(personDao));
        httpServer.addController("/api/people", new ListPeopleController(personDao));
        logger.info("Starting http://localhost:{}/index.html",httpServer.getPort());

    }

    private static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")) {
            properties.load(fileReader);
        }
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(properties.getProperty("dataSource.url",
                "jdbc:postgresql://localhost:5432/person_db2"
        ));

        dataSource.setUser(properties.getProperty("dataSource.user",
                "person_dbuser2"
        ));

        dataSource.setPassword(properties.getProperty("dataSource.password"));

        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
