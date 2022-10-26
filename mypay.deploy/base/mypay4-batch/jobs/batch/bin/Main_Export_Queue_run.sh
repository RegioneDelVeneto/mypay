#!/bin/sh

perl -p -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg; s/\$\{([^}]+)\}//eg' /conf/export-properties_1.0.properties > /conf/export.properties

export ROOT_PATH="/batch/jobs/BatchExport/BatchExport"

ls -latr  /conf
ls -latr $ROOT_PATH/lib/*

cat -A /conf/flusso-tassonomia-service.xml
cat -A /conf/export.properties


java -Xms256M -Xmx1024M -cp $ROOT_PATH/*:$ROOT_PATH/../lib/*:/batch/lib/* pa_talend.main_export_queue_0_1.Main_Export_Queue --context_param directory_talend=/E45
