package no.kristiania;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpMessage {
    public static String messageBody;
    public String startLine;
    public final Map<String, String> headerFields = new HashMap<>();

    public HttpMessage(Socket socket) throws IOException {
         // leser statusLine
         startLine = HttpMessage.readLine(socket);
         // leser headers
         readHeaders(socket);
         // Dersom request har body(altså en post request), så må vi parse den. Det har den visst vi har content-length i header. Da skal vi lese hele body som kommer etter headere. Bruker readBodyBytes()
         if(headerFields.containsKey("Content-Length")) {
            messageBody = HttpMessage.readBodyBytes(socket, getContentLength());
        }

    }
    //********************************* klassemetoder *************


    // ****leser hele body, som kommer fra responsen til server. Husk at vi alltid har body i http response.
    static String readBodyBytes(Socket socket, int contentLength) throws IOException {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            buffer.append((char)socket.getInputStream().read());
        }
        return buffer.toString();
    }


    private void readHeaders(Socket socket) throws IOException {
        // skal parse Headerlines fra server. altså det før body. Lagrer Field og value i hashmap
        String headerLine;
        while (!(headerLine = HttpMessage.readLine(socket) ).isBlank() ){ // ved blank linje er headers ferdig, da kommer body.
            int colonPos = headerLine.indexOf(":");
            String headerField = headerLine.substring(0,colonPos);
            String headerValue = headerLine.substring(colonPos+1).trim(); // trim fjerner WS fra begge sider.
            headerFields.put(headerField,headerValue);  // lagres i hashmap

        }
    }

    // ***** readLine: Leser en linje i response headers(input).
    // Første er status line (f.eks "HTTP/1.1 200 OK").
    // Skal den lese flere linjer må den settes i en while der den kalles.
    static String readLine(Socket socket) throws IOException {
        StringBuilder result = new StringBuilder();
        int c;
        // skal lese en linje, dvs til CR
        while ((c = socket.getInputStream().read()) != '\r' ){
            result.append((char)c); // konverterer til char
        }
        // må lese \n for å havne riktig til neste linje
        int expectedNewLine = socket.getInputStream().read();
        assert expectedNewLine == '\n';
        return result.toString();
    }

    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }
    public String getHeader(String headerName) {
        return headerFields.get(headerName);// headerFields sin get av headerName
    }

}




