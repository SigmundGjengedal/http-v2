package no.kristiania;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        // når vi skal ha en server bruker vi ServerSocket. Den åpner en port på vår pc.
        ServerSocket serverSocket = new ServerSocket(10080);

        Socket clientSocket = serverSocket.accept();

        // på en socket har vi input og outputStream.
        // Vi starter på input, vi ser hva klienten sender til server(meg)

    }
}
