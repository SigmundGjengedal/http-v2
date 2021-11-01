package no.kristiania.http;

import no.kristiania.person.Person;
import no.kristiania.person.PersonDao;
import no.kristiania.person.RoleDao;
import no.kristiania.person.TestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {
    // fields:
    //må starte en server som skal få request
    private final HttpServer server = new HttpServer(0);

    // rar måte for constr å kaste EX videre.
    HttpServerTest() throws IOException {
    }

    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {

        //client skal connecte til samme server:
        HttpGetClient client = new HttpGetClient("localhost",server.getPort(),"/non-existing");
        assertEquals(404,client.getStatusCode());
    }

    @Test
    void ShouldRespondWithRequestTargetIn404() throws IOException {
        HttpGetClient client = new HttpGetClient("localhost",server.getPort(),"/non-existing");
        assertEquals("File not found: /non-existing",client.getMessageBody());
    }

    @Test
    void shouldRespondWith200ForKnownRequestTarget() throws IOException {
        HttpGetClient client = new HttpGetClient("localhost",server.getPort(),"/api/hello");
        assertAll(
                () -> assertEquals(200, client.getStatusCode()),
                () -> assertEquals("text/html", client.getHeader("Content-Type")),
                () -> assertEquals("<p>Hello World</p>", client.getMessageBody())
        );
    }

    @Test
    void shouldHandleMoreThanOneRequests() throws IOException {
        assertEquals(200, new HttpGetClient("localhost", server.getPort(), "/api/hello").getStatusCode());
        assertEquals(200, new HttpGetClient("localhost", server.getPort(), "/api/hello").getStatusCode());



    }


    @Test
    void shouldEchoQueryParameter() throws IOException {
        HttpGetClient client = new HttpGetClient(
                "localhost",
                server.getPort(),
                "/api/hello?firstName=Test&lastName=Gjengedal"
        );
        assertEquals("<p>Hello Gjengedal, Test</p>", client.getMessageBody());
    }

    @Test
    void shouldServeFiles() throws IOException {
        // lager server og sier til server: se etter filer på disken i denne katalogen
        Paths.get("target/test-classes");

        //  lager innhold, og skriver innhold til en fil i den katalogen
        String fileContent = "A file created at " + LocalTime.now();
        Files.write(Paths.get("target/test-classes/example-file.txt"),fileContent.getBytes());

        // sier til serveren at jeg skal hente den fila, og at jeg da forventer å få innholdet i den  fila.
        HttpGetClient client = new HttpGetClient("localhost",server.getPort(),"/example-file.txt");
        assertEquals(fileContent,client.getMessageBody());
        assertEquals("text/plain",client.getHeader("Content-Type"));
    }

    @Test
    void shouldUseFileExtensionForContentType() throws IOException {
        // lager server og sier til server: se etter filer på disken i denne katalogen
        Paths.get("target/test-classes");

        //  lager innhold, og skriver innhold til en fil i den katalogen
        String fileContent = "<p> Hello</p>";
        Files.write(Paths.get("target/test-classes/example-file.html"),fileContent.getBytes());

        // sier til serveren at jeg skal hente den fila, og at jeg da forventer å få innholdet i den  fila.
        HttpGetClient client = new HttpGetClient("localhost", server.getPort(),"/example-file.html");
        // vil at den skal respondere med text/html
        assertEquals("text/html",client.getHeader("Content-Type"));

    }

    @Test
     void shouldReturnRolesFromServer() throws IOException, SQLException {
        // gitt at serveren min er satt opp med en del roller vi skal returnere, så er det disse rollene vi skal returnere:
        RoleDao roleDao = new RoleDao(TestData.testDataSource());
        roleDao.save("Teacher");
        roleDao.save("Student");

        server.addController("/api/roleOptions",new RoleOptionsController(roleDao));


        HttpGetClient client = new HttpGetClient("localhost",server.getPort(),"/api/roleOptions");
        assertEquals(
            "<option value=1>Teacher</option><option value=2>Student</option>",
                client.getMessageBody()
                );
    }

    @Test
    void shouldCreateNewPerson() throws IOException, SQLException {
        PersonDao personDao = new PersonDao(TestData.testDataSource());
        server.addController("/api/newPerson",new AddPersonController(personDao));


        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/newPerson",
                "lastName=Gjengedal&firstName=Test"
        );
        assertEquals(200,postClient.getStatusCode());
          Person person = personDao.listAll().get(0);
        assertEquals("Test", person.getFirstName());
    }

}