package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
            messageBody = HttpMessage.readBody(socket, getContentLength());
        }

    }

    public HttpMessage(String startLine, String messageBody){
        this.startLine = startLine;
        this.messageBody = messageBody;
    }


    //********************************* klassemetoder ***********************************

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

    public static Map<String, String> parseRequestParameters(String query) {
        // parser ut queryParameterene. De som gir fornavn og etternavn. & tegnet skiller forskjellig key value sett.( s=x&z=n )
        Map<String, String> queryMap = new HashMap<>();
        for (String queryParameter : query.split("&")) {
            int equalsPos = queryParameter.indexOf('=');
            String parameterName = queryParameter.substring(0,equalsPos);
            String parameterValue = queryParameter.substring(equalsPos +1);
            queryMap.put(parameterName,parameterValue);
        }
        return queryMap;
    }

    // leser headers
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

    // ****leser hele body.
    // Leser den som bytes. Returner som en string via toString.
    static String readBody(Socket socket, int contentLength) throws IOException {
        byte[] buffer = new byte[contentLength];
        for (int i = 0; i < contentLength; i++) {
            buffer[i] = (byte) socket.getInputStream().read();
        }
        return new String(buffer , StandardCharsets.UTF_8);
    }


    // gettere:
    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }
    public String getHeader(String headerName) {
        return headerFields.get(headerName);// headerFields sin get av headerName
    }

    // skriver tilbake en httpmessage til socketen
    public void write(Socket socket) throws IOException {
        String response = startLine + "\r\n" +
                "Content-Length: " + messageBody.getBytes().length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                messageBody;
        //  sender responsen ut fra clientSocket
        socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
    }
}




