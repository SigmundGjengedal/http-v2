package no.kristiania;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpMessage {


    public static String messageBody;
    public String startLine;
    static Map<String, String> headerFields = new HashMap<>();

    public HttpMessage(Socket socket) throws IOException {

         // leser statusLine
         startLine = HttpMessage.readLine(socket);
         // leser headers
         readHeaders(socket);
         // Dersom request har body(altså en post request), så må vi parse den. Det har den visst vi har content-length i header. Da skal vi lese hele body som kommer etter headere. Bruker readBodyBytes()
         if(headerFields.containsKey("Content-Length")) {
            messageBody = HttpMessage.readBody(socket, getContentLength());
        }

    }
    //********************************* klassemetoder *************


    // ***** Leser requestline, eller responsline.
    // (f.eks "HTTP/1.1 200 OK").
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

        // returner stringen:
        return result.toString();
    }

    // leser headers
    public static void readHeaders(Socket socket) throws IOException {
        // skal parse Headerlines fra server. altså det før body. Lagrer Field og value i hashmap
        String headerLine;
        while (!(headerLine = HttpMessage.readLine(socket) ).isBlank() ){ // ved blank linje er headers ferdig, da kommer body.
            int colonPos = headerLine.indexOf(":");
            String headerField = headerLine.substring(0,colonPos);
            String headerValue = headerLine.substring(colonPos+1).trim(); // trim fjerner WS fra begge sider.
            headerFields.put(headerField,headerValue);  // lagres i hashmap

        }
    }

    // ****leser hele body.
    // Leser den som bytes. Returner som en string via toString.
    static String readBody(Socket socket, int contentLength) throws IOException {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            body.append((char)socket.getInputStream().read());
        }
        return body.toString();
    }


    // gettere:
    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }
    public static String getHeader(String headerName) {
        return headerFields.get(headerName);// headerFields sin get av headerName
    }
    public static String getMessageBody() {
        return messageBody;
    }

}




