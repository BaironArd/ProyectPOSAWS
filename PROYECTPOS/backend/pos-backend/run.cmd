@echo off
setlocal

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo Ejecutando backend POS con Java 21...
echo.

java -Dmaven.multiModuleProjectDirectory="%~dp0" -classpath "%~dp0.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain spring-boot:run

endlocal
