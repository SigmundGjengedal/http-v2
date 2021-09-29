package no.kristiania;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpMessage {
    public String startLine;
    public final Map<String, String> headerFields = new HashMap<>();

    public HttpMessage(Socket socket) throws IOException {
     startLine = HttpMessage.readLine(socket);
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
}
