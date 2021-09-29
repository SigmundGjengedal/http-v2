package no.kristiania;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    //********************************** fields. Disse tilhører det klienten får tilbake fra server.
    private final int statusCode;
    private HttpMessage httpMessage;

    //************************************* constructor
    public HttpClient(String host , int port, String requestTarget) throws IOException {

        // **************   sender request **************

        // Må ha socket for å connecte til server. Connecter socket til host og port som angitt.
        Socket socket1 = new Socket(host,port);

        // Skal skrive en http-request(en string). Requestline + to første linjene i Request Headers
        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        // sender den til server, som output fra klient. Sender stringen som bytes
        socket1.getOutputStream().write(request.getBytes());


        // **************  leser respons **************
        httpMessage = new HttpMessage(socket1);

        //Skal lese statuskode fra server: HelpMethod ReadLine gir oss tilbake hele status line. Den består av tre deler splittet av mellomrom.
        String[] statusLineSplitted = httpMessage.startLine.split(" "); // [protocol, statuscode, statusmessage]
        this.statusCode = Integer.parseInt(statusLineSplitted[1]);    // Vi er bare interessert i statuscoden(f.eks 200).
        // leser headers

        // skal lese hele body som kommer etter headere. Bruker readBytes()
        HttpMessage.messageBody = readBodyBytes(socket1, getContentLength());
    }// end of constructor





    // ****leser hele body, som kommer fra responsen til server. Husk at vi alltid har body i http response.
    private String readBodyBytes(Socket socket, int contentLength) throws IOException {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            buffer.append((char)socket.getInputStream().read());
        }
        return buffer.toString();
    }


    //************************************** gettere : henter ulike deler av respons fra server.
    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String headerName) {
        return httpMessage.headerFields.get(headerName);// headerFields sin get av headerName
    }

    // henter de ut fra headerFieldet content-length, som alltid sier hvor stort innholdet er.
    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }

    public String getMessageBody() {
        return httpMessage.messageBody;
    }
}
