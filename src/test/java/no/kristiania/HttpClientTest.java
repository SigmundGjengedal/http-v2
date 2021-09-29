package no.kristiania;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpClientTest {

   @Test
   void shouldReturnStatesCode() throws IOException {
      assertEquals(200, new HttpClient("httpbin.org",80, "/html").getStatusCode()) ;
      assertEquals(404, new HttpClient("httpbin.org",80, "/no-such-page").getStatusCode()) ;
   }

   @Test
   void shouldReturnHeaders() throws IOException {
      HttpClient client = new HttpClient("httpbin.org",80,"/html");
      assertEquals("text/html; charset=utf-8",client.getHeader("Content-Type"));
   }


   @Test
   void shouldReadBody() throws IOException {
      HttpClient client = new HttpClient("httpbin.org",80,"/html");
      assertTrue(client.getMessageBody().startsWith("<!DOCTYPE html>"),
              "Expected HTML: " + client.getMessageBody());
   }
}
