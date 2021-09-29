package no.kristiania;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HttpServer {

    private final ServerSocket serverSocket;
    private Path rootDirectory;
    private List<String> roles = new ArrayList<>();

    public HttpServer(int serverPort) throws IOException {
        // må lytte til en severSocket:
        serverSocket = new ServerSocket(serverPort);
        // i en separat tråd, kjør handleClients
        new Thread(this::handleClients).start();
    }

    // leser fra klients connection,  og svarer den med riktig response.
    private void handleClients(){

        try {
            while(true){
              handleClient();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void handleClient() throws IOException {
        // må accepte request fra client: kobler altså outputten fra client, til inputten til servere:
        Socket clientSocket = serverSocket.accept();
        // må lese requestline
        String[] requestLine = HttpClient.readLine(clientSocket).split(" ");
        // henter ut hele requestTarget fra requestLine
        String requestTarget = requestLine[1];

        // splitter requestTarget i fileTarget og query
        int questionPos = requestTarget.indexOf('?');
        String fileTarget; // skal deles i filetarget og query
        String query = null;
        if (questionPos!= -1) { //  om vi har query
            fileTarget = requestTarget.substring(0,questionPos);
            query = requestTarget.substring(questionPos+1); // hvis vi har et spørsmålstegn, har vi en query med name og value.
        } else { // om vi ikke har query.
            fileTarget = requestTarget;
        }

        // kontrollstuktur iht hva fileTarget og query er:
        if(fileTarget.equals("/hello")){
            String yourName = "World";
            if (query != null){
                yourName = query.split("=")[1]; // henter ut value fra input
            }
            // lager svar til klienten for filetarget = "/hello":
            String responseText ="<p>Hello "+ yourName+ "</p>";
            String response = "HTTP/1.1 200 ok\r\n" +
                    "Content-Length: " +responseText.getBytes().length + "\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Connection: close\r\n" +
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
                        "Connection: close\r\n" +
                        "\r\n"+
                        responseText;
                //  sender responsen ut fra clientSocket
                clientSocket.getOutputStream().write(response.getBytes());
                return;
            }
            String responseText = "File not found: " + requestTarget;

            String response = "HTTP/1.1 404 Not found\r\n" +
                    "Content-Length: " + responseText.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseText;
            clientSocket.getOutputStream().write(response.getBytes());
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

        // 1: Vi kan ta input: leser den første linja fra klienten,som er requestlinen fra chrome
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

    public <E> void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
