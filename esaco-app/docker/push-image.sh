#!/bin/bash
ESACO_IMAGE=${ESACO_IMAGE:-indigoiam/esaco}

cd ../../
POM_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[')
GIT_COMMIT_SHA=$(git rev-parse --short HEAD)
GIT_BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD | sed 's#/#_#g')

if [[ -n ${DOCKER_REGISTRY_HOST} ]]; then
  docker tag ${ESACO_IMAGE} ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}
  docker tag ${ESACO_IMAGE}:${POM_VERSION}-${GIT_COMMIT_SHA} ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}:${POM_VERSION}-${GIT_COMMIT_SHA}
  docker tag ${ESACO_IMAGE}:${POM_VERSION}-latest ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}:${POM_VERSION}-latest

  docker push ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}
  docker push ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}:${POM_VERSION}-${GIT_COMMIT_SHA}
  docker push ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}:${POM_VERSION}-latest
else
  docker push ${ESACO_IMAGE}
  docker push ${ESACO_IMAGE}:${POM_VERSION}-${GIT_COMMIT_SHA}
  docker push ${ESACO_IMAGE}:${POM_VERSION}-latest
  docker push ${ESACO_IMAGE}:${GIT_BRANCH_NAME}-latest
fi

