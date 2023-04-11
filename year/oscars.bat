@echo off

REM *** Oscars - Options ***
REM Oscars.bat Oscars = Enter the winners in the contest
REM Oscars.bat BallotReader = Continuously download the ballots
REM Oscars.bat BallotReader emails = Download the emails

set OSCARS_HOME=..\..\..\..\Java\Oscars
java -classpath %OSCARS_HOME%;%OSCARS_HOME%/bin;%OSCARS_HOME%/lib/* oscars.%*
