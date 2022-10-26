#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batchInviaEsitoPagamentoPush-properties_1.0.properties > /conf/batchInviaEsitoPagamentoPush.properties
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batch-data.properties > /conf/batch.properties

export ROOT_TALEND="/batch"
export ROOT_PATH="/batch/jobs/BatchInviaEsitoPagamentoPush/BatchInviaEsitoPagamentoPush"


java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.batchinviaesitopagamentopush_0_1.BatchInviaEsitoPagamentoPush --context_param directory_talend=$ROOT_TALEND
