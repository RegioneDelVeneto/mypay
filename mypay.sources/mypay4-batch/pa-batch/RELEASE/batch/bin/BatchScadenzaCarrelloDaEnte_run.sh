#!/bin/sh

export JAVA_HOME="__TAG_BATCH_JAVA_HOME_PATH__"
export ROOT_TALEND="__TAG_BATCH_ROOT_PATH__"
export JAVA="$JAVA_HOME/bin/java"
export ROOT_PATH="$ROOT_TALEND/jobs/BatchScadenzaCarrelloDaEnte/BatchScadenzaCarrelloDaEnte"

cd $ROOT_PATH

$JAVA -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/* pa_talend.batchscadenzacarrellodaente_0_1.BatchScadenzaCarrelloDaEnte --context_param directory_talend=$ROOT_TALEND
