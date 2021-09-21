package no.kristiania;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class HttpServer {

    private final ServerSocket serverSocket;
    private Path rootDirectory;

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

            // kontrollstuktur på requesTarget:

            if(requestTarget.equals("/hello")){
                String responseText ="Hello World";
                String response = "HTTP/1.1 200 ok\r\n" +
                        "Content-Length: " +responseText.getBytes().length + "\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n"+
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());
            }else{
                // det serveren skal svare klienten
                String responseText ="File not found: " + requestTarget;
                String response = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: " +responseText.getBytes().length + "\r\n" +
                        "\r\n"+
                        responseText;
                //  sender response ut fra clientSocket
                clientSocket.getOutputStream().write(response.getBytes());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        new HttpServer(1991);

        /*

        // Kode før vi lagde handleClients:
        // når vi skal ha en server bruker vi ServerSocket. Den åpner en port på vår pc.
        // Vi er serveren. Vi trenger derfor ikke angi serveren, men porten. Velger 10080.IOEx om porten er tatt fra før.
        ServerSocket serverSocket = new ServerSocket(1990);// skriv i chrome: localhost:8080

        // nå må vi ventet på svar klientetn, vi får vi tilbake noe(et socket object lik det klienten sendte), det må vi akseptere:
        Socket clientSocket = serverSocket.accept();

        // 1: Vi kan ta input: leser den første linja fra server,som er requestlinen fra chrome
        String requestLine = HttpClient.readLine(clientSocket);
        System.out.println(requestLine);

        // 2: kan gi output. Sender en http-respons til chrome
        String body = "<h1> Hello World !!!</h1>";
        String contentType = "text/html; charset=utf-8";

        String responseToClient =  "HTTP/1.1 200 OK\r\n"+
                "Content-Length: " + body.getBytes().length +"\r\n" +
                "Content type: "+ contentType+"\r\n"+
                "Connection: close\r\n"+
                "\r\n" +
                body;
        clientSocket.getOutputStream().write((responseToClient).getBytes()); // må sendes som bytes

            */


        /* skriver ut headerlinjene som chrome sendte ut.
        String headerLine;
        while(!(headerLine = HttpClient.readLine(clientSocket)).isBlank()) {
            System.out.println(headerLine);
        }*/
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setRoot(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }
}
