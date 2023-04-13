@echo off

REM *** Oscars - Options ***
REM Oscars.bat Oscars = Enter the winners in the contest
REM Oscars.bat Ballot = Continuously download the ballots
REM Oscars.bat Ballot emails = Download the emails

set OSCARS_HOME=..\..\..\..\Java\Oscars
java -classpath %OSCARS_HOME%;%OSCARS_HOME%/bin;%OSCARS_HOME%/lib/* oscars.%*
