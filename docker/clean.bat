REM ===================================================================
REM                 DOCKER CLEAN-UP SYSTEM: STARTING
REM ===================================================================
cd
docker system df
docker-compose -f dev-compose.yml down --remove-orphans
docker container prune --force
docker system prune --volumes --force
docker network prune --force
docker builder prune --all --force
docker image rm pauloportfolio/api-web
docker image rm pauloportfolio/api-db
::  THE IMAGE pauloportfolio/api IS NOT DELETED, when compose is --forcing-recreating
docker image rm pauloportfolio/api
docker image ls
docker system df

:: RE-SETTING JDK8 AS DEFAULT
set JAVA_HOME=C:\Program Files\Java\jdk-8.0.282.8-hotspot

:: CLOSING ALL CMD-SCREENS
:: TASKKILL /F /IM cmd.exe /T

:: exit
REM ===================================================================
REM                 DOCKER CLEAN-UP SYSTEM: ENDING
REM ===================================================================