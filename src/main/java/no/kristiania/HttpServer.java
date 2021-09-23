package no.kristiania;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

            int questionPos = requestTarget.indexOf('?');
            String fileTarget;
            String query = null;
            if (questionPos!= -1) { //
                fileTarget = requestTarget.substring(0,questionPos);
                query = requestTarget.substring(questionPos+1); // hvis vi har et spørsmålstegn, har vi en query
            } else {
                fileTarget = requestTarget;
            }

            if(fileTarget.equals("/hello")){
                String yourName = "world";
                if (query != null){
                    yourName = query.split("=")[1]; // henter ut navn fra input
                }
                String responseText ="<p>Hello "+ yourName+ "</p>";

                String response = "HTTP/1.1 200 ok\r\n" +
                        "Content-Length: " +responseText.getBytes().length + "\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n"+
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());
            }else{

                if (rootDirectory!= null &&  Files.exists(rootDirectory.resolve(requestTarget.substring(1)))){
                    // finner fila som request target peker til:
                    String responseText = Files.readString(rootDirectory.resolve(requestTarget.substring(1)));

                    // default verdi
                    String contentType = "text/plain";
                    // men endres om...
                    if (requestTarget.endsWith(".html")){
                        contentType = "text/html";
                    }
                    String response = "HTTP/1.1 200 ok\r\n" +
                            "Content-Length: " +responseText.getBytes().length + "\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "\r\n"+
                            responseText;
                    //  sender responsen ut fra clientSocket
                    clientSocket.getOutputStream().write(response.getBytes());
                    return;
                }
                String responseText = "File not found: " + requestTarget;

                String response = "HTTP/1.1 404 Not found\r\n" +
                        "Content-Length: " + responseText.length() + "\r\n" +
                        "\r\n" +
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        HttpServer httpServer = new HttpServer(1990);
        // setter et root directory. Velger working directory,der vi er nå,  også kjent som "." Legger index.html rett i root.
        httpServer.setRoot(Paths.get("."));
        // i chrome: localhost:1990/index.html

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
