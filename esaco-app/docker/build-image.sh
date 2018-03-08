#!/bin/bash
set -ex
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}"  )" && pwd  )"

if [[ -z ${ESACO_JAR} ]]; then
  for f in ${DIR}/../target/esaco-app-*.jar; do
    ESACO_JAR=${f}
    break
  done
fi

if [[ ! -r ${ESACO_JAR} ]]; then
  echo "Please set the ESACO_JAR env variable so that it points to the ESACO jar location"
  exit 1
fi

echo "Building image using jar from ${ESACO_JAR}"

ESACO_IMAGE=${ESACO_IMAGE:-indigoiam/esaco}

cd ${DIR}
cp ${ESACO_JAR} esaco-app.jar

docker build --rm=true -t ${ESACO_IMAGE} .
rm esaco-app.jar
