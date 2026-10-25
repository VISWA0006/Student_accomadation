@echo off
echo ==================================================================
echo  Student Accommodation Management System (PostgreSQL Profile)
echo  Server Port : 8085
echo  Credentials : admin / Admin123!
echo ==================================================================
echo Opening browser in 6 seconds...
start /b cmd /c "timeout /t 6 /nobreak >nul && start http://localhost:8085/login"
mvn -f accommodation-main\pom.xml spring-boot:run "-Dspring-boot.run.arguments=--app.security.admin.username=admin --app.security.admin.password=Admin123!"
