package no.kristiania;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpClientTest {

   @Test
   void shouldReturnStatesCode() throws IOException {
      assertEquals(200, new HttpClient("httpbin.org",80, "/html").getStatusCode()) ;
      assertEquals(404, new HttpClient("httpbin.org",80, "/no-such-page").getStatusCode()) ;
   }


}
