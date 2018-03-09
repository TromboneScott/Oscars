export OSCARS_HOME=../../../../Java/Oscars
rm category/* player/* 2> /dev/null
java -classpath $OSCARS_HOME/bin:$OSCARS_HOME/lib/* oscars.Oscars
