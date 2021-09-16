package no.kristiania;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpClientTest {
   @Test
   void shouldReturn200(){
      assertEquals(200, HttpClient.dummyTest()) ;
   }

}
