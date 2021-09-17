package no.kristiania;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpClient {

    private final int statusCode;

    public HttpClient(String host , int port, String requestTarget) throws IOException {
        // socket for å connecte til server. Connecter til host og port som angitt.
        Socket socket1 = new Socket(host,port);

        // skriver en http-request(en string). De tre første linjene i Request Headers
        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        // sender den til server.Output fra klient. Sender stringen som bytes
        socket1.getOutputStream().write(request.getBytes());

        //HM ReadLine gir oss hele status line. Den består av tre deler. Vi er bare interessert i statuscoden(f.eks 200).
        String[] statusLine = readLine(socket1).split(" ");
        this.statusCode = Integer.parseInt(statusLine[1]);
        }

    // hjelpemetoder(HM)

    // Skal lese tilbake første linje i response headers(input), som er status line (f.eks "HTTP/1.1 200 OK")
    private String readLine(Socket socket) throws IOException {
        StringBuilder result = new StringBuilder();
        int c;
        // skal lese en linje, dvs til CR
        while ((c = socket.getInputStream().read()) != '\r' ){
            result.append((char)c); // konverterer til char
        }
        return result.toString();
    }


    // gettere
    public int getStatusCode() {
        return statusCode;
    }
}
