package no.kristiania;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    //********************************** fields. Disse tilhører det klienten får tilbake fra server.
    private final int statusCode;
    private final Map<String, String> headerFields = new HashMap<>();
    private String messageBody; // ikke optional!
    private HttpMessage httpMessage;

    //************************************* constructor
    public HttpClient(String host , int port, String requestTarget) throws IOException {

        // **************   sender request **************

        // Må ha socket for å connecte til server. Connecter til host og port som angitt.
        Socket socket1 = new Socket(host,port);

        // Skal skrive en http-request(en string). Requestline + to første linjene i Request Headers
        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        // sender den til server, som output fra klient. Sender stringen som bytes
        socket1.getOutputStream().write(request.getBytes());


        // **************  leser respons **************



        //Skal lese statuskode fra server: HelpMethod ReadLine gir oss tilbake hele status line. Den består av tre deler splittet av mellomrom.
        String[] statusLineSplitted = readLine(socket1).split(" "); // [protocol, statuscode, statusmessage]
        this.statusCode = Integer.parseInt(statusLineSplitted[1]);    // Vi er bare interessert i statuscoden(f.eks 200).

        // skal parse Headerlines fra server. altså det før body. Lagrer Field og value i hashmap
        String headerLine;
        while (!(headerLine = readLine(socket1)) .isBlank()){ // ved blank linje er headers ferdig, da kommer body.
              int colonPos = headerLine.indexOf(":");
              String headerField = headerLine.substring(0,colonPos);
              String headerValue = headerLine.substring(colonPos+1).trim(); // trim fjerner WS fra begge sider.
              headerFields.put(headerField,headerValue);  // lagres i hashmap
        }

        // skal lese hele body som kommer etter headere. Bruker readBytes()
        this.messageBody = readBodyBytes(socket1, getContentLength());
    }// end of constructor

    //********************************* hjelpemetoder(HM)

    // ****leser hele body, som kommer fra responsen til server. Husk at vi alltid har body i http response.
    private String readBodyBytes(Socket socket, int contentLength) throws IOException {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            buffer.append((char)socket.getInputStream().read());
        }
        return buffer.toString();
    }

    // *****Leser en linje i response headers(input).
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


    //************************************** gettere : henter ulike deler av respons fra server.
    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String headerName) {
        return headerFields.get(headerName);// headerFields sin get av headerName
    }

    // henter de ut fra headerFieldet content-length, som alltid sier hvor stort innholdet er.
    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }

    public String getMessageBody() {
        return messageBody;
    }
}
