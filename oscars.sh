export OSCARS_HOME=.
rm category/* player/*
java -classpath $OSCARS_HOME/bin:$OSCARS_HOME/lib/* oscars.Oscars
