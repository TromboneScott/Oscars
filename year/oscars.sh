export OSCARS_HOME=../../../../Java/Oscars
rm category/* player/*
java -classpath $OSCARS_HOME/bin:$OSCARS_HOME/lib/* oscars.Oscars
