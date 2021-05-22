echo on
REM SETTING JDK11 AS DEFAULT
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.10+9

cd ..
call mvn clean package -DskipTests
cd docker-script-start-files

docker system df

REM DOCKER CLEAN-ALL ORPHANS
docker-compose -f ../dev-compose.yml down --remove-orphans
REM docker-compose -f ../test-compose.yml down --remove-orphans

docker container prune --force
docker container rm $(docker container ls -q)

docker system prune --volumes --force
docker volume rm $(docker volume ls -q)

docker image prune --force
docker image rm pauloportfolio/api-web

REM DOCKER LISTING IMAGES + SYSTEM
docker system prune --force
docker image ls
docker system df

REM START THE COMPOSE CONTAINERS
docker-compose -f ../dev-compose.yml up --build --force-recreate


pause
