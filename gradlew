#!/usr/bin/env sh
#############################################################################
# Gradle wrapper script (minimal standard wrapper)
#############################################################################
APP_BASE_NAME="gradle"
APP_HOME_DIR=""
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
APP_HOME_DIR=`dirname "$PRG"`/.
exec java -jar "$APP_HOME_DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
