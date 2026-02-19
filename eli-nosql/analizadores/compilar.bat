@echo off
setlocal
cd /d %~dp0

set OUT=..\src\main\java\analizadores
if not exist "%OUT%" mkdir "%OUT%"

echo Lexer con JFlex -> %OUT%
java -jar jflex-1.9.1.jar -d "%OUT%" Lexico.jflex
if errorlevel 1 goto error

echo Parser CUP -> %OUT%
java -jar java-cup-11b.jar -parser Sintactico -symbols sym -destdir "%OUT%" Sintactico.cup
if errorlevel 1 goto error

echo.
echo OK - Generados en %OUT%
pause
exit /b 0

:error
echo.
echo ERROR al generar analizadores **
pause
exit /b 1
