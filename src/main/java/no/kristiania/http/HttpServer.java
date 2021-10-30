package no.kristiania.http;

import no.kristiania.person.Person;
import no.kristiania.person.PersonDao;
import no.kristiania.person.RoleDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {

    private final ServerSocket serverSocket;
    private List<Person> people = new ArrayList<>();
    private RoleDao roleDao;

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
        }catch(IOException | SQLException e){ // om noe går galt på nettverket, f.eks ugyldig host.
            e.printStackTrace();
        }
    }

    // parser request fra client: Ser foreløpig kun på første linje, altså requestline.
    private void handleClient() throws IOException, SQLException {
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
        if(requestTarget.equals("/api/listPeople")){
            // String messageBody = "DETTE ER LISTEN";
            String text = "";
            String messageBody = people.toString();
            writeOkResponse(clientSocket, messageBody,"text/html");
        }
        else if (fileTarget.equals("/api/newPerson")) {
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            Person person = new Person();
            person.setFirstName(queryMap.get("firstName"));
            person.setLastName(queryMap.get("lastName"));
            people.add(person);
            writeOkResponse(clientSocket,"it is done", "text/html");

        } else if (fileTarget.equals("/api/roleOptions")) {
            String responseText = "";
            int value = 1;
            for(String role : roleDao.listAll()){
                responseText += "<option value=" +(value++) +">" + role + "</option>";
            }
            writeOkResponse(clientSocket, responseText, "text/html");

        }else{
            // satt opp for å handtere filstruktur med jar fil. Angir hvor vi finner koden og leser/parser bytes.
            InputStream fileResource = getClass().getResourceAsStream(fileTarget);
            if (fileResource != null){
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                fileResource.transferTo(buffer);
                String responseText = buffer.toString();

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

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public List<Person> getPeople() {
        return people;
    }



 // ****************** MAIN

    public static void main(String[] args) throws IOException {
        // i chrome: localhost:1991/index.html
        HttpServer httpServer = new HttpServer(1991);
        // hvor vi finner rollene
        httpServer.setRoleDao(new RoleDao(createDataSource()));

    }

    private static DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/person_db2");
        dataSource.setUser("person_dbuser2");
        dataSource.setPassword("k%3'`(?Qu?");
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
