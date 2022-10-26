#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batchImportDisabilitaDovuti-properties_1.0.properties > /conf/batchImportDisabilitaDovuti.properties
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batch-data.properties > /conf/batch.properties

export ROOT_PATH="/batch/jobs/BatchImportDisabilitaDovuti/BatchImportDisabilitaDovuti"

java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.batchimportdisabilitadovuti_0_1.BatchImportDisabilitaDovuti  --context_param directory_talend=/E45
