package no.kristiania;

import java.io.IOException;
import java.net.Socket;

public class HttpPostClient {
    private final HttpMessage httpMessage;
    private final int statusCode;

    public HttpPostClient(String host, int port, String requestTarget, String contentBody) throws IOException { // klipt og limt httpclient, med noen endringer.
        // **************  konstruerer request **************
        // Må ha socket for å connecte til server. Connecter socket til host og port som angitt.
        Socket socket1 = new Socket(host,port);
        // Skal skrive en http-request(en string). Requestline + to første linjene i Request Headers
        String request = "POST " + requestTarget + " HTTP/1.1\r\n" + // httpmetode, rt, protocol
                "Host: " + host + "\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + contentBody.length() + "\r\n" +
                "\r\n"+
                contentBody;
        // sender den til server, som output fra klient. Sender stringen som bytes
        socket1.getOutputStream().write(request.getBytes());


        // **************  leser respons **************
        httpMessage = new HttpMessage(socket1); // må ha objekt for å bruke metodene.
        //Skal lese statuskode fra server: HelpMethod ReadLine gir oss tilbake hele status line. Den består av tre deler splittet av mellomrom.
        String[] statusLineSplitted = httpMessage.startLine.split(" "); // [protocol, statuscode, statusmessage]
        this.statusCode = Integer.parseInt(statusLineSplitted[1]);    // Vi er bare interessert i statuscoden(f.eks 200).
    } // end of constructor

    public int getStatusCode() {
        return statusCode;
    }
}
