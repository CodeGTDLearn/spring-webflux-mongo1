echo on

REM DOCKER CLEAN-UP SYSTEM
cd docker
cd
docker system df
docker-compose -f dev-compose.yml down --remove-orphans
docker container prune --force
docker system prune --volumes --force
docker network prune --force
docker builder prune --all --force
docker image rm pauloportfolio/api-web
docker image rm pauloportfolio/api-db
docker image rm pauloportfolio/api
docker image ls
docker system df

REM RE-SETTING JDK8 AS DEFAULT
set JAVA_HOME=C:\Program Files\Java\jdk-8.0.282.8-hotspot

REM CLOSING ALL CMD-SCREENS
TASKKILL /F /IM cmd.exe /T

exit