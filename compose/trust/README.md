# Trustanchor container

This folder holds a trustanchor container which populates volumes
that can be mounted in the other services such to establish a trust 
framework based on X.509 certificates created on-the-fly.

The trustanchor container populates the following volumes

* `/trust-anchors`: contains the `igi-test-ca` CA certificate, issuing X.509
  server/user certificates (created on-the-fly), which is usually mounted
  in `/etc/grid-security/certificates`
* `/etc/pki/tls/certs`: it is the bundle for system certificates plus
  the `igi-test-ca` one
* `/certs`: contains the X.509 server certificate, emitted by the
  `igi-test-ca`.

**Tip:** Some docker version uses a buildx image which caches some layer of the build image to speedup the container start time. This is in conflict with the creation of on-the-fly certificates. To disable this behavior, first stop and remove the container

```bash
$ docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED             STATUS                         PORTS     NAMES
<container-id>   moby/buildkit:buildx-stable-1   "buildkitd"              2 weeks ago         Up 7 days                                buildx_buildkit_practical_dewdney0
$ docker stop <container-id>
$ docker rm <container-id>
```

and also the volume created by buildx

```bash
$ docker volume ls
DRIVER    VOLUME NAME
local     buildx_buildkit_practical_dewdney0_state
$ docker volume rm buildx_buildkit_practical_dewdney0_state
buildx_buildkit_practical_dewdney0_state
```

Then set

```bash
export DOCKER_BUILDKIT=0
```

(may be that you have to add it to your `~/.bashrc`).