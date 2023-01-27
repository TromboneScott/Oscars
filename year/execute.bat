@echo off
set OSCARS_HOME=..\..\..\..\Java\Oscars
set /p URL=<%OSCARS_HOME%\ResponsesURL.txt
java -classpath %OSCARS_HOME%/bin;%OSCARS_HOME%/lib/* oscars.%1 "%URL%"
