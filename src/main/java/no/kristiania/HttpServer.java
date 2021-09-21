package no.kristiania;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private final ServerSocket serverSocket;

    public HttpServer(int serverPort) throws IOException {
        // må lytte til en severSocket:
        serverSocket = new ServerSocket(serverPort);
        // i en separat tråd, kjør handleClients
        new Thread(this::handleClients).start();
    }

    // leser fra klients connection,  og svarer den med 404 not found
    private void handleClients(){

        try { // venter på svar
            // må accepte: settes til clientSocket. kobler altså outputten fra server til inputten til client:
            Socket clientSocket = serverSocket.accept();

            // må lese requestline
            String[] requestLine = HttpClient.readLine(clientSocket).split(" ");
            // henter ut requestTarget fra requestLine
            String requestTarget = requestLine[1];
            // det serveren skal svare klienten
            String responseText ="File not found: " + requestTarget;
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: " +responseText.getBytes().length + "\r\n" +
                    "\r\n"+
                    responseText;
            //  sender response ut fra clientSocket
            clientSocket.getOutputStream().write(response.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        /*HTTPCLIENT: Vi fikk verdier fra chrome, som vi parsa, altså leste og tolket
        (intelliJ er på klientsiden(chrome), kobler til en server via httpbin.org).

         HTTPSERVER: Nå er det vår jobb å sende de samme verdiene tilbake til chrome(intelliJ er på serversiden, som chrome connectes til).
         Skriver den enkleste koden som kan få chrome til å vise en tekst-side.
        */

        // når vi skal ha en server bruker vi ServerSocket. Den åpner en port på vår pc.
        // Vi er serveren. Vi trenger derfor ikke angi serveren, men porten. Velger 10080.IOEx om porten er tatt fra før.
        ServerSocket serverSocket = new ServerSocket(1990);// skriv i chrome: localhost:8080

        // nå må vi ventet på svar klientetn, vi får vi tilbake noe(et socket object lik det klienten sendte), det må vi akseptere:
        Socket clientSocket = serverSocket.accept();

        // 1: Vi kan ta input: leser den første linja fra server,som er requestlinen fra chrome
        String requestLine = HttpClient.readLine(clientSocket);
        System.out.println(requestLine);

        // 2: kan gi output. Sender en http-respons til chrome
        String body = "<h1> Hellååå World !!!</h1>";
        String contentType = "text/html; charset=utf-8";

        String responseToClient =  "HTTP/1.1 200 OK\r\n"+
                "Content-Length: " + body.getBytes().length +"\r\n" +
                "Content type: "+ contentType+"\r\n"+
                "Connection: close\r\n"+
                "\r\n" +
                body;
        clientSocket.getOutputStream().write((responseToClient).getBytes()); // må sendes som bytes

        /* skriver ut headerlinjene som chrome sendte ut.
        String headerLine;
        while(!(headerLine = HttpClient.readLine(clientSocket)).isBlank()) {
            System.out.println(headerLine);
        }*/
    }
}
