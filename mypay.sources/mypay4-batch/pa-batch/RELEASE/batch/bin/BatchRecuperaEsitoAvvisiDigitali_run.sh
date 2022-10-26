#!/bin/sh

export JAVA_HOME="__TAG_BATCH_JAVA_HOME_PATH__"
export ROOT_TALEND="__TAG_BATCH_ROOT_PATH__"
export JAVA="$JAVA_HOME/bin/java"
export ROOT_PATH="$ROOT_TALEND/jobs/BatchRecuperaEsitoAvvisiDigitali/Main_Batch_Recupera_Esito_Avvisi_Digitali"

cd $ROOT_PATH

$JAVA -DHOSTNAME=$HOSTNAME -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/* avvisatura_batch_pa.main_batch_recupera_esito_avvisi_digitali_0_1.Main_Batch_Recupera_Esito_Avvisi_Digitali --context_param directory_talend=$ROOT_TALEND 