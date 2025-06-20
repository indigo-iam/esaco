#!/bin/bash

apt update
apt install -y apache2 libapache2-mod-auth-openidc

a2enmod auth_openidc cgi
a2enconf openidc

apt install -y curl
# This is required since the curl installation has overwritten
# the CA bundle including igi-test-ca
cp /debian-ssl/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt 

apache2ctl -D FOREGROUND -e debug