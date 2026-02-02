@echo off
REM Gradle wrapper batch script (minimal)
SETLOCAL
set PRG=%~dp0%~nx0
set APP_HOME=%~dp0
"%JAVA_HOME%\bin\java" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
ENDLOCAL
