@echo off
echo ==================================================================
echo  Student Accommodation Management System (Demo Profile)
echo  Server Port : 8086
echo  Credentials : admin / DemoAdmin123!
echo ==================================================================
echo Opening browser in 6 seconds...
start /b cmd /c "timeout /t 6 /nobreak >nul && start http://localhost:8086/login"
mvn -f accommodation-main\pom.xml spring-boot:run "-Dspring-boot.run.profiles=h2" "-Dspring-boot.run.arguments=--server.port=8086"
