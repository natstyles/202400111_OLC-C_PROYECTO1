@echo off
cd /d %~dp0

echo Lexer con JFlex
java -jar jflex-1.9.1.jar Lexico.jflex
if errorlevel 1 goto error

echo Parser CUP
java -jar java-cup-11b.jar -parser Sintactico -symbols sym Sintactico.cup
if errorlevel 1 goto error

echo.
echo Archivos generados
dir /b *.java
pause
exit /b 0

:error
echo.
echo ERROR al generar analizadores **
pause
exit /b 1
