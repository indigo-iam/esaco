#!/bin/sh
set -ex

if [ ! -e "openssl.conf" ]; then
  >&2 echo "The configuration file 'openssl.conf' doesn't exist in this directory"
  exit 1
fi

certs_dir=/certs
ta_dir=/etc/grid-security/certificates
ca_bundle_prefix=/etc/pki

rm -rf "${certs_dir}"
mkdir -p "${certs_dir}"

export CA_NAME=igi_test_ca
export X509_CERT_DIR="${ta_dir}"

make_ca.sh

# Create server certificates.
# This must match the openssl configuration in conf.d/*.conf
server_name=test_example
make_cert.sh ${server_name}

cp igi_test_ca/certs/${server_name}.cert.pem "${certs_dir}"/hostcert.pem
cp igi_test_ca/certs/${server_name}.key.pem "${certs_dir}"/hostkey.pem
chmod 600 "${certs_dir}"/hostcert.pem
chmod 400 "${certs_dir}"/hostkey.pem

chown 1000:1000 "${certs_dir}"/*

make_crl.sh
install_ca.sh igi_test_ca "${ta_dir}"

# Add igi-test-ca to system certificates
ca_bundle="${ca_bundle_prefix}"/tls/certs

echo -e "\n# igi-test-ca" >> "${ca_bundle}"/ca-bundle.crt
cat "${ta_dir}"/igi_test_ca.pem >> "${ca_bundle}"/ca-bundle.crt
