#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batchChiediCopiaEsito-properties_1.0.properties > /conf/batchChiediCopiaEsito.properties
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batch-data.properties > /conf/batch.properties

export ROOT_PATH="/batch/jobs/BatchChiediCopiaEsitoParallel/BatchChiediCopiaEsitoParallel"

java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.batchchiedicopiaesitoparallel_0_1.BatchChiediCopiaEsitoParallel --context_param directory_talend=/E45
