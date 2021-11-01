package no.kristiania.http;

import no.kristiania.person.Person;
import no.kristiania.person.PersonDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

public class AddPersonController implements HttpController {
    private PersonDao personDao;
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public AddPersonController(PersonDao personDao) {

        this.personDao = personDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        // logger.info(queryMap.get("firstName"));
        Person person = new Person();
        // decoding
        String decodedFirstName = URLDecoder.decode(queryMap.get("firstName"), StandardCharsets.UTF_8);
        logger.info(decodedFirstName);
        String decodedLastName = URLDecoder.decode(queryMap.get("lastName"), StandardCharsets.UTF_8);
        // setting values to object
        person.setFirstName(decodedFirstName);
        person.setLastName(decodedLastName);
        personDao.save(person);

        return new HttpMessage("HTTP/1.1 200 OK", "It is done");
    }
}
