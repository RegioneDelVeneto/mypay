#!/bin/sh

JAVA_HOME="/opt/jdk1.7.0"
export JAVA_HOME

M2_HOME="/opt/apache-maven"
export M2_HOME

"$M2_HOME"/bin/mvn hibernate3:hbm2java hibernate3:hbm2hbmxml
