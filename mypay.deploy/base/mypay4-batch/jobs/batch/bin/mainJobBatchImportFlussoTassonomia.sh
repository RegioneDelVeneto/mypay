#!/bin/sh

PRG="$0"
PRGDIR=`dirname "$PRG"`
JOB_HOME="$PRGDIR/.."

cd $JOB_HOME

ROOT_PATH="/batch/jobs/BatchImportFlussoTassonomia"

echo "java -DHOSTNAME=$HOSTNAME -Xmx1024M -cp /conf:$JOB_HOME/lib/*:$ROOT_PATH/lib/* it.regioneveneto.mygov.payment.pa.batch.BatchImportFlussoTassonomia" 2>&1

ls -latr  /conf
ls -latr $JOB_HOME/lib/*
ls -latr $ROOT_PATH/lib/*

cat -A /conf/flusso-tassonomia-service.xml
cat -A /conf/batch.properties
cat -A /conf/batchImportFlussoTassonomia.properties

java -DHOSTNAME=$HOSTNAME -Xmx1024M -cp /conf:$JOB_HOME/lib/*:$ROOT_PATH/lib/* it.regioneveneto.mygov.payment.pa.batch.BatchImportFlussoTassonomia

