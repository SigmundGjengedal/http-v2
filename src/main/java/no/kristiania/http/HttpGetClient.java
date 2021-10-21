package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;

public class HttpGetClient {
    //********************************** fields. Disse tilhører det klienten får tilbake fra server.
    private final int statusCode;
    private HttpMessage httpMessage;

    //************************************* constructor
    public HttpGetClient(String host , int port, String requestTarget) throws IOException {
        // **************   konstruerer request **************
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
        //Skal lese statuskode fra server:
        String[] statusLineSplitted = httpMessage.startLine.split(" "); // [protocol, statuscode, statusmessage]
        this.statusCode = Integer.parseInt(statusLineSplitted[1]);    // Vi er bare interessert i statuscoden(f.eks 200).

    }// end of constructor


    //************************************** gettere : henter ulike deler av respons fra server.
    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String headerName) {
        return httpMessage.headerFields.get(headerName);// headerFields sin get av headerName
    }

    public String getMessageBody() {
        return httpMessage.messageBody;
    }
}
