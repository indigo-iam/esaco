#!/bin/bash
set -ex
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}"  )" && pwd  )"

if [[ -z ${ARGUS_OIDC_CLIENT_JAR} ]]; then
  for f in ${DIR}/../target/oidc-client-*.jar; do
    ARGUS_OIDC_CLIENT_JAR=${f}
    break
  done
fi

if [[ ! -r ${ARGUS_OIDC_CLIENT_JAR} ]]; then
  echo "Please set the ARGUS_OIDC_CLIENT_JAR env variable so that it points to the oidc client jar location"
  exit 1
fi

echo "Building image using jar from ${ARGUS_OIDC_CLIENT_JAR}"

cd ${DIR}
cp ${ARGUS_OIDC_CLIENT_JAR} oidc-client.jar

docker build --rm=true -t argus/argus-oidc-client .
rm oidc-client.jar
