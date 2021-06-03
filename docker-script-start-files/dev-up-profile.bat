echo on
REM SETTING JDK11 AS DEFAULT
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.10+9

REM MAVEN CLEAN PACKAGE
cd ..
call mvn clean package -DskipTests

REM DOCKER CLEAN-UP SYSTEM
cd docker-script-start-files
docker system df
docker-compose -f ../dev-compose.yml down --remove-orphans
docker container prune --force
docker system prune --volumes --force
docker network prune --force
docker builder prune --all --force
docker image rm pauloportfolio/api-web
docker image rm pauloportfolio/api-db
rem  THE IMAGE pauloportfolio/api IS NOT DELETED, 'cause compose is --forcing-recreating
REM docker image rm pauloportfolio/api
docker image ls
docker system df

docker scan --version --json --group-issues

REM DOCKER-COMPOSE UP
docker-compose -f ../dev-compose.yml up --build --force-recreate

pause

rem docker image prune --force
rem docker system prune -a --force
