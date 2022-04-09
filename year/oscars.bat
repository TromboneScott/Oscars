@set OSCARS_HOME=../../../../Java/Oscars
@echo Deleting any old data
@del/q category\* player\* rank\*
@echo Starting Oscars
@java -classpath %OSCARS_HOME%/bin;%OSCARS_HOME%/lib/* oscars.Oscars %*
