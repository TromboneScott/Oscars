export OSCARS_HOME=../../../../Java/Oscars
java -classpath "$OSCARS_HOME/bin;$OSCARS_HOME/lib/*" oscars.Ballot `cat $OSCARS_HOME/ResponsesURL.txt`
