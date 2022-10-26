#!/bin/sh

PRG="$0"
PRGDIR=`dirname "$PRG"`
JOB_HOME="$PRGDIR/.."

cd $JOB_HOME

java -DHOSTNAME=$HOSTNAME -Xmx1024M -cp /conf:$JOB_HOME/lib/* it.regioneveneto.mygov.payment.pa.batch.BatchChiediCopiaEsito $1

