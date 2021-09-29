[![Java CI with Maven](https://github.com/SigmundGjengedal/http-v2/actions/workflows/maven.yml/badge.svg)](https://github.com/SigmundGjengedal/http-v2/actions/workflows/maven.yml)

## lecture 6
* [] styling
  - legger til style.css
    -sette rootdir i main for å angi html fila.
   - fjerner doctype linja øverst i html dokument!
   -  
* [] handle more than one client
   - lagde test med to httpclienter
   - extracta handleclients,i server. Satt den i en whileløkke med true.

* [] feilhåndtering når vi har en aktiv server
    - må lukke connection til socket. må sette "connection: close" i alle response
      slik at chrome lukker socket og åpner en ny for hver request.

* [] refactor -> httpMessage class
  - DRY : extract methods, spesielt den som gir 200 respons.Og den som parser query.

   - refactor HttpPostClient - lagde via TDD i httpServerTest: shouldCreateNewPerson()
* [] process POST requests from form  
    - (method = “post” i form index.html
    
* [] URL encoding

* [] Make executable JAR
