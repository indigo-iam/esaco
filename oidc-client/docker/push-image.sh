#!/bin/bash
ARGUS_OIDC_CLIENT_IMAGE=${ARGUS_OIDC_CLIENT_IMAGE:-argus/argus-oidc-client}

if [[ -n ${DOCKER_REGISTRY_HOST} ]]; then
  docker tag ${DOCKER_REGISTRY_HOST}/${ARGUS_OIDC_CLIENT_IMAGE}
  docker push ${DOCKER_REGISTRY_HOST}/${ARGUS_OIDC_CLIENT_IMAGE}
else
  docker push ${ARGUS_OIDC_CLIENT_IMAGE}
fi

