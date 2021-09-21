package no.kristiania;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {
    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        //må starte en server som skal få request
        HttpServer server = new HttpServer(1991);
        //client skal connecte til samme server:
        HttpClient client = new HttpClient("localhost",server.getPort(),"/non-existing");
        assertEquals(404,client.getStatusCode());
    }

    @Test
    void ShouldRespondWithRequestTargetIn404() throws IOException {
        HttpServer server = new HttpServer(1992);
        HttpClient client = new HttpClient("localhost",server.getPort(),"/non-existing");
        assertEquals("File not found: /non-existing",client.getMessageBody());
    }

    @Test
    void shouldRespondWith200ForKnownRequestTarget() throws IOException {
        HttpServer server = new HttpServer(1993);
        HttpClient client = new HttpClient("localhost",server.getPort(),"/hello");
        assertAll(
                () -> assertEquals(200, client.getStatusCode()),
                () -> assertEquals("text/html", client.getHeader("Content-Type")),
                () -> assertEquals("<p>Hello World</p>", client.getMessageBody())
        );
    }

    @Test
    void shouldServeFiles() throws IOException {
        // lager server og sier til server: se etter filer på disken i denne katalogen
        HttpServer server = new HttpServer(0);
        server.setRoot(Paths.get("target/test-classes"));
        //  lager innhold, og skriver innhold til en fil i den katalogen
        String fileContent = "A file created at " + LocalTime.now();
        Files.write(Paths.get("target/test-classes/example-file.txt"),fileContent.getBytes());

        // sier til serveren at jeg skal hente den fila, og at jeg da forventer å få innholdet i den  fila.
        HttpClient client = new HttpClient("localhost",server.getPort(),"/example-file.txt");
        assertEquals(fileContent,client.getMessageBody());


    }
}