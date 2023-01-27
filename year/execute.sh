export OSCARS_HOME=../../../../Java/Oscars
java -classpath "$OSCARS_HOME;$OSCARS_HOME/bin;$OSCARS_HOME/lib/*" oscars.$1 `cat $OSCARS_HOME/ResponsesURL.txt`
