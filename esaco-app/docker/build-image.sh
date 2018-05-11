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

cd ${DIR}/../../

if [[ -z ${POM_VERSION} ]]; then
  POM_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[')
fi

GIT_COMMIT_SHA=$(git rev-parse --short HEAD)
GIT_BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD | sed 's#/#_#g')

cd ${DIR}
cp ${ESACO_JAR} esaco-app.jar

docker build --rm=true -t ${ESACO_IMAGE} .

docker tag ${ESACO_IMAGE} ${ESACO_IMAGE}:${POM_VERSION}-${GIT_COMMIT_SHA}
docker tag ${ESACO_IMAGE} ${ESACO_IMAGE}:${POM_VERSION}-latest

if [ -n "${GIT_BRANCH_NAME}" ]; then
  docker tag ${ESACO_IMAGE} ${ESACO_IMAGE}:${GIT_BRANCH_NAME}-latest
fi

rm esaco-app.jar
