package no.kristiania;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) throws IOException {

        /*HTTPCLIENT: Vi fikk verdier fra chrome, som vi parsa, altså leste og tolket
        (intelliJ er på klientsiden(chrome), kobler til en server via httpbin.org).

         HTTPSERVER: Nå er det vår jobb å sende de samme verdiene tilbake til chrome(intelliJ er på serversiden, som chrome connectes til).
         Skriver den enkleste koden som kan få chrome til å vise en tekst-side.
        */

        // når vi skal ha en server bruker vi ServerSocket. Den åpner en port på vår pc.
        // Vi er serveren. Vi trenger derfor ikke angi serveren, men porten. Velger 10080.IOEx om porten er tatt fra før.
        ServerSocket serverSocket = new ServerSocket(8080);// skriv i chrome: localhost:8080

        // nå får vi tilbake noe(et socket object lik det klienten sendte), det må vi akseptere:
        Socket clientSocket = serverSocket.accept();

        // 1: Vi kan ta input: leser den første linja fra server,som er requestlinen fra chrome
        String requestLine = HttpClient.readLine(clientSocket);
        System.out.println(requestLine);

        // 2: kan gi output. Sender en http-respons til chrome
        String messageBody = "<h1> Hello World !!!</h1>";

        String responseToClient =  "HTTP/1.1 200 OK\r\n"+
                "Content-Length: " + messageBody.length() +"\r\n" +
                "Connection: close\r\n"+
                "\r\n" +
                messageBody;
        clientSocket.getOutputStream().write((responseToClient).getBytes()); // må sendes som bytes

        // skriver ut headerlinjene som chrome sendte ut.
        String headerLine;
        while(!(headerLine = HttpClient.readLine(clientSocket)).isBlank()) {
            System.out.println(headerLine);
        }
    }
}
