REM ===================================================================
REM                 DOCKER-COMPOSE SYSTEM: STARTING
REM ===================================================================
:: SETTING JDK11 AS DEFAULT
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.10+9

:: MAVEN CLEAN PACKAGE
cd ..
call mvn clean package -DskipTests

:: DOCKER CLEAN-UP SYSTEM
cd docker
call clean.bat

docker scan --version --json --group-issues

:: DOCKER-COMPOSE UP
docker-compose -f dev-compose.yml up --build --force-recreate

pause
REM ===================================================================
REM                 DOCKER-COMPOSE SYSTEM: FINISHING
REM ===================================================================