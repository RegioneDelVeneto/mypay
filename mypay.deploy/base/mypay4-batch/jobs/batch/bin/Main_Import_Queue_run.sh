#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/import-properties_1.0.properties > /conf/import.properties

export ROOT_PATH="/batch/jobs/BatchImport/BatchImport"

ls -latr  /conf
ls -latr $ROOT_PATH/lib/*

cat -A /conf/import.properties


java -Xms256M -Xmx1024M -Xss64M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.main_import_queue_0_1.Main_Import_Queue --context_param directory_talend=/E45
