#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batchInvioEmailEsito-properties_1.0.properties > /conf/batchInvioEmailEsito.properties
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batch-data.properties > /conf/batch.properties

export ROOT_PATH="/batch/jobs/BatchInvioEmailEsito/BatchInvioEmailEsito"

java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.batchinvioemailesito_0_1.BatchInvioEmailEsito --context_param directory_talend=/E45
