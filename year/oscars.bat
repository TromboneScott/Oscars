@echo off

REM *** Oscars - Options ***
REM oscars.bat Oscars = Enter the winners in the contest
REM oscars.bat Ballots = Continuously download the ballots
REM oscars.bat Ballots emails = Download the emails

set OSCARS_HOME=..\..\..\..\Java\Oscars
java -classpath %OSCARS_HOME%;%OSCARS_HOME%/bin;%OSCARS_HOME%/lib/* oscars.%*
