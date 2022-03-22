export OSCARS_HOME=../../../../Java/Oscars
echo Deleting any old data
rm category/* player/* rank/* 2> /dev/null
echo Starting Oscars
java -classpath "$OSCARS_HOME/bin;$OSCARS_HOME/lib/*" oscars.Oscars
