#!/bin/bash
ESACO_IMAGE=${ESACO_IMAGE:-indigoiam/esaco}

if [[ -n ${DOCKER_REGISTRY_HOST} ]]; then
  docker tag ${ESACO_IMAGE} ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}
  docker push ${DOCKER_REGISTRY_HOST}/${ESACO_IMAGE}
else
  docker push ${ESACO_IMAGE}
fi

