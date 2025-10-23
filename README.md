# P3-Temp-Name
### TL;DR
1. Open the project
2. Create or start MySQL Docker container with 
   1. To create, run in terminal: docker-compose up -d
   2. To start, run in terminal: docker-compose start
   3. To stop, run in terminal: docker-compose stop
3. Start the server, by running in terminal: .\mvnw spring-boot:run
4. Open the web page
   1. For the webpage: http://localhost:8080/
   2. For the phpMyAdmin: http://localhost:8081/

### Description
Make sure to have Docker Desktop to run MySQL docker for web server https://www.docker.com/products/docker-desktop/ <br>
To run database, turn on MySQL and phpMyAdmin in Docker Desktop or write docker-compose start or docker-compose stop to turn dockers on or off. If they cannot be found, docker-compose up -d in the terminal to create dockers. <br>
Afterwards, write .\mvnw spring-boot:run in terminal to start server, which can be accessed in http://localhost:8080/ and phpMyAdmin in http://localhost:8081/