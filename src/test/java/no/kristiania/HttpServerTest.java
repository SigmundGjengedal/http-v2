package no.kristiania;

import org.junit.jupiter.api.Test;

import java.io.IOException;

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
        //må starte en server som skal få request
        HttpServer server = new HttpServer(1992);
        //client skal connecte til samme server:
        HttpClient client = new HttpClient("localhost",server.getPort(),"/non-existing");
        assertEquals("File not found: /non-existing",client.getMessageBody());
    }
}