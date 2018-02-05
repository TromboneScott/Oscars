@set OSCARS_HOME=.
@del/q category\* player\*
@java -classpath %OSCARS_HOME%/bin;%OSCARS_HOME%/lib/* oscars.Oscars