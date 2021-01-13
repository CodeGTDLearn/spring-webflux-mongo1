echo on
REM SETTING JDK11 AS DEFAULT
set JAVA_HOME=C:\Program Files\java\AdoptOpenJDK-11.0.8+10

cd ..
call mvn clean package -DskipTests
cd docker-script-start-files

REM DOCKER CLEAN-ALL ORPHANS
docker-compose -f ../dev-compose.yml down --remove-orphans
docker-compose -f ../test-compose.yml down --remove-orphans

docker container prune --force
docker container rm $(docker container ls -q)

docker system prune --volumes --force
docker volume rm $(docker volume ls -q)

docker image prune --force
docker image rm pauloportfolio/api-web

REM DOCKER LISTING IMAGES + SYSTEM
docker system df
docker image ls

REM START THE COMPOSE CONTAINERS
docker-compose -f ../test-compose.yml up --build --force-recreate

pause
