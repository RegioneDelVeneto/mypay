#!/bin/sh

PRG="$0"
PRGDIR=`dirname "$PRG"`
JOB_HOME="$PRGDIR/.."

cd $JOB_HOME

ROOT_PATH="/batch/jobs/BatchImportDovutiOperatori"

java -DHOSTNAME=$HOSTNAME -Xmx1024M -cp /conf:$JOB_HOME/lib/*:$ROOT_PATH/lib/* it.regioneveneto.mygov.payment.pa.batch.BatchImportDovutiOperatori

