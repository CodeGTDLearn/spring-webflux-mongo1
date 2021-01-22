echo on

REM DOCKER CLEAN-ALL ORPHANS
docker-compose -f ../dev-compose.yml down --remove-orphans
docker-compose -f ../test-compose.yml down --remove-orphans

docker container prune --force
docker container rm $(docker container ls -q)

docker system prune --volumes --force
docker volume rm $(docker volume ls -q)

docker image prune --force
docker image rm pauloportfolio/api

REM DOCKER LISTING IMAGES + SYSTEM
docker system df
docker image ls

REM RE-SETTING JDK8 AS DEFAULT
set JAVA_HOME=C:\Program Files\Java\jdk-8.0.282.8-hotspot

REM CLOSING ALL CMD-SCREENS
TASKKILL /F /IM cmd.exe /T

exit