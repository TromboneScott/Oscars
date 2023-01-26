export OSCARS_HOME=../../../../Java/Oscars
java -classpath "$OSCARS_HOME/bin;$OSCARS_HOME/lib/*" oscars.Emails `cat $OSCARS_HOME/ResponsesURL.txt`
