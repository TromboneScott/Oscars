@set OSCARS_HOME=../../../../Java/Oscars
@del/q category\* player\*
@java -classpath %OSCARS_HOME%/bin;%OSCARS_HOME%/lib/* oscars.Oscars