package no.kristiania.http;

import no.kristiania.person.Person;
import no.kristiania.person.RoleDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final HashMap<String, HttpController> controllers = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);


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

        if (controllers.containsKey(fileTarget)) {
            HttpMessage response = controllers.get(fileTarget).handle(httpMessage);
            response.write(clientSocket);
            return;
        }

        // ******************* kontrollstuktur :  iht hva fileTarget og query ble i forrige steg:

        else if(fileTarget.equals("/api/hello")) {
            String yourName = "World";
            if (query != null) {
                Map<String, String> queryMap = HttpMessage.parseRequestParameters(query);
                // henter ut noen av variablene
                yourName = queryMap.get("lastName") + ", " + queryMap.get("firstName");
            }
            // lager svar til klienten for filetarget = "/hello":
            String responseText = "<p>Hello " + yourName + "</p>";
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




 // ****************** MAIN

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1800);
        // hvor vi finner rollene
        new RoleDao(createDataSource());

        logger.info("Starting http://localhost:{}/index.html",httpServer.getPort());

    }

    private static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")) {
            properties.load(fileReader);
        }
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(properties.getProperty("dataSource.url",
                "jdbc:postgresql://localhost:5432/person_db2"
        ));

        dataSource.setUser(properties.getProperty("dataSource.user",
                "person_dbuser2"
        ));

        dataSource.setPassword(properties.getProperty("dataSource.password"));

        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }

    public void addController(String path, HttpController controller) {
        controllers.put(path, controller);
    }
}
