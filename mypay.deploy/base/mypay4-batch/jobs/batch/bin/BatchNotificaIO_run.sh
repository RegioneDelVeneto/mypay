#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batchNotificaIO-properties_1.0.properties > /conf/batchNotificaIO.properties
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batch-data.properties > /conf/batch.properties

export ROOT_PATH="/batch/jobs/BatchNotificaIO/BatchNotificaIO"

java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.batchnotificaio_0_1.BatchNotificaIO --context_param directory_talend=/E45
