echo Starting Oscars
export OSCARS_HOME=../../../../Java/Oscars
rm category/* player/* rank/* 2> /dev/null
java -classpath $OSCARS_HOME/bin:$OSCARS_HOME/lib/* oscars.Oscars
