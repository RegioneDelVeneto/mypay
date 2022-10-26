#!/bin/sh

export JAVA_HOME="__TAG_BATCH_JAVA_HOME_PATH__"

PRG="$0"
PRGDIR=`dirname "$PRG"`
JOB_HOME="$PRGDIR/.."

cd $JOB_HOME

$JAVA_HOME/bin/java -DHOSTNAME=$HOSTNAME -Xmx1024M -cp $JOB_HOME/conf/BatchFlussoRendicontazione:$JOB_HOME/lib/* it.regioneveneto.mygov.payment.nodoregionalefesp.batch.BatchChiediFlussoRendicontazione
