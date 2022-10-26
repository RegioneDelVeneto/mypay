#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /batch/conf/BatchImportFlussoTassonomia/batchImportFlussoTassonomia-properties_1.0.properties > /conf/batchImportFlussoTassonomia.properties
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /batch/conf/BatchImportFlussoTassonomia/batch-data.properties > /conf/batch.properties

cp -r /batch/conf/BatchImportFlussoTassonomia/messages.properties /batch/conf/BatchImportFlussoTassonomia/flusso-tassonomia-service.xml /batch/conf/BatchImportFlussoTassonomia/log4j* /batch/conf/BatchImportFlussoTassonomia/velocity /conf/


export ROOT_PATH="/batch/jobs/BatchImportFlussoTassonomia/BatchImportFlussoTassonomia"

java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.batchimportflussotassonomia_0_1.BatchImportFlussoTassonomia --context_param directory_talend=/E45
