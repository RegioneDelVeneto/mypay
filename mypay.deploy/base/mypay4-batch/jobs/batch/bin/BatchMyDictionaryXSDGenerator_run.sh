#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batchMyDictionaryXSDGenerator-properties_1.0.properties > /conf/batchMyDictionaryXSDGenerator.properties
perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/batch-data.properties > /conf/batch.properties

export ROOT_PATH="/batch/jobs/BatchMyDictionaryXSDGenerator/BatchMyDictionaryXSDGenerator"

java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.batchmydictionaryxsdgenerator_0_1.BatchMyDictionaryXSDGenerator --context_param directory_talend=/E45
