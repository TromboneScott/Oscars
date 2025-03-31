#!/bin/bash

export OSCARS_HOME=../../../../Java/Oscars

usage() {
  echo "Usage: $0 [-b] [-e]"
  echo "  -b = Just coninuously download the ballots"
  echo "  -e = Just get the emails"
  echo "  -h = Just show Usage"
}

class="Oscars"
while getopts beh opt
do
  case $opt in
    b) class="ballot.Ballots";;
    e) class="ballot.Ballots emails";;
    h) usage
       exit 0;;
    ?) usage >&2
       exit 1;;
  esac
done
shift $((OPTIND-1))

java -classpath "$OSCARS_HOME;$OSCARS_HOME/bin;$OSCARS_HOME/lib/*" oscars.$class $*