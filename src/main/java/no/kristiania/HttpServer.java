package no.kristiania;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {

    private final ServerSocket serverSocket;
    private Path rootDirectory;
    private List<String> roles = new ArrayList<>();
    private List<Person> people = new ArrayList<>();
    private Map<Integer,Person> allPersons =new HashMap<>();


    public HttpServer(int serverPort) throws IOException {
        // må lytte til en severSocket på samme port som clienten:
        serverSocket = new ServerSocket(serverPort);
        // i en separat tråd, kjør handleClients
        new Thread(this::handleClients).start();
    }

    // leser fra klients connection(requesline), og responderer etter å ha tolket requestline. Bruker hjelpemetode handleClient()
    private void handleClients(){

        try {
            while(true){ // evig. Er for å holde serveren oppe.
              handleClient();
            }
        }catch(IOException e){ // om noe går galt på nettverket, f.eks ugyldig host.
            e.printStackTrace();
        }
    }

    // parser request fra client: Ser foreløpig kun på første linje, altså requestline.
    private void handleClient() throws IOException {
        // må accepte request fra client,  og opprette kontakt.  Denne brukes for å sende svar tilbake.
        Socket clientSocket = serverSocket.accept();

        // oppretter instanse av httpMessage for å hente ut requestline. Må ha metodene i httpMessage,
        HttpMessage httpMessage = new HttpMessage(clientSocket);
        // må parse requestline. Bruker readline uten while, da blir det bare en linje. Splitter på mellomrom, og lagrer i et array.
        String[] requestLine = httpMessage.startLine.split(" ");
        // parser ut hele requestTarget fra requestLine.
        String requestTarget = requestLine[1];// requestline =  [HTTP-METHOD, requestTarget, HTTP-PROTOCOL]

        // parser requestTarget i fileTarget og query ( kommer inn som f.eks /hello?firstName=geir&lastName=hansen)
        int questionPos = requestTarget.indexOf('?');
        String fileTarget; // skal deles i filetarget og query
        String query = null;
        if (questionPos!= -1) { //  om vi har query
            fileTarget = requestTarget.substring(0,questionPos);
            query = requestTarget.substring(questionPos+1); // hvis vi har et spørsmålstegn, har vi en query med name og value.
        } else { // om vi ikke har query.
            fileTarget = requestTarget;
        }

        // ******************* kontrollstuktur :  iht hva fileTarget og query ble i forrige steg:

        if(fileTarget.equals("/api/hello")) {
            String yourName = "World";
            if (query != null) {
                Map<String, String> queryMap = parseRequestParameters(query);
                // henter ut noen av variablene
                yourName = queryMap.get("lastName") + ", " + queryMap.get("firstName");
            }
            // lager svar til klienten for filetarget = "/hello":
            String responseText = "<p>Hello " + yourName + "</p>";
            writeOkResponse(clientSocket, responseText, "text/html");

        }
        if(requestTarget.equals("/api/products")){
            // String messageBody = "DETTE ER LISTEN";
            String text = "";
            String messageBody = returnProductMap(allPersons, text);
            writeOkResponse(clientSocket, messageBody,"text/html");
        }
        else if (fileTarget.equals("/api/newPerson")) {
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            Person person = new Person();
            person.setFirstName(queryMap.get("firstName"));
            people.add(person);
            writeOkResponse(clientSocket,"it is done", "text/html");

        } else if (fileTarget.equals("/api/roleOptions")) {
            String responseText = "";
            int value = 1;
            for(String role : roles){
                responseText += "<option value=" +(value++) +">" + role + "</option>";
            }
            writeOkResponse(clientSocket, responseText, "text/html");

        }else{

            if (rootDirectory!= null &&  Files.exists(rootDirectory.resolve(requestTarget.substring(1)))){
                // finner fila som filetarget peker til:
                String responseText = Files.readString(rootDirectory.resolve(requestTarget.substring(1)));
                // default verdi
                String contentType = "text/plain";
                // men endres om...
                if (requestTarget.endsWith(".html")){
                    contentType = "text/html";
                }
                writeOkResponse(clientSocket, responseText, contentType);
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

    //************************** HM

    private Map<String, String> parseRequestParameters(String query) {
        // parser ut queryParameterene. De som gir fornavn og etternavn. & tegnet skiller forskjellig key value sett.( s=x&z=n )
        Map<String, String> queryMap = new HashMap<>();
        for (String queryParameter : query.split("&")) {
            int equalsPos = queryParameter.indexOf('=');
            String parameterName = queryParameter.substring(0,equalsPos);
            String parameterValue = queryParameter.substring(equalsPos +1);
            queryMap.put(parameterName,parameterValue);
        }
        return queryMap;
    }

    private void writeOkResponse(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 200 ok\r\n" +
                "Content-Length: " + responseText.getBytes().length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseText;
        //  sender responsen ut fra clientSocket
        clientSocket.getOutputStream().write(response.getBytes());
    }

    // getter og setters
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setRoot(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Person> getPeople() {
        return people;
    }

    public String returnProductMap(Map map, String messageBody){
        for (int i = 1; i < map.size()+1; i++) {
            messageBody += "<p>" + i + ": " + map.get(i).toString() + "</p>";
        }
        return messageBody;
    }

 // ****************** MAIN

    public static void main(String[] args) throws IOException {
        // i chrome: localhost:1990/index.html
        HttpServer httpServer = new HttpServer(1990);
        //hvor vi finner html koden: setter et root directory. Velger working directory,der vi er nå,  også kjent som "." Legger index.html rett i root.
        httpServer.setRoot(Paths.get("."));
        // hvor vi finner rollene
        httpServer.setRoles(List.of("Student", "Teaching assistant","Teacher"));

    }
}
