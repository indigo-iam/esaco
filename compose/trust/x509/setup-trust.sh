#!/bin/sh
set -ex

if [ ! -e "openssl.conf" ]; then
  >&2 echo "The configuration file 'openssl.conf' doesn't exist in this directory"
  exit 1
fi

certs_dir=/certs
ta_dir=/etc/grid-security/certificates

rm -rf "${certs_dir}"
mkdir -p "${certs_dir}"

export CA_NAME=igi_test_ca
export X509_CERT_DIR="${ta_dir}"

make_ca.sh

# Create server certificates.
# This must match the openssl configuration in conf.d/*.conf
for c in apache_test_example esaco_test_example iam1_test_example iam2_test_example; do
  make_cert.sh ${c}
  cp igi_test_ca/certs/${c}.* "${certs_dir}"
done

chmod 600 "${certs_dir}"/*.cert.pem
chmod 400 "${certs_dir}"/*.key.pem
chmod 600 "${certs_dir}"/*.p12
chown 101:101 "${certs_dir}"/*

make_crl.sh
install_ca.sh igi_test_ca "${ta_dir}"

# Create a volume for igi-test-ca + system certificates bundle
system_bundle=/etc/pki/tls/certs/ca-bundle.crt
buffer_bundle=/debian-ssl/ca-certificates.crt
mkdir -p /debian-ssl

cp "${system_bundle}" "${buffer_bundle}"
echo -e "\n# igi-test-ca" >> "${buffer_bundle}"
cat "${ta_dir}"/igi_test_ca.pem >> "${buffer_bundle}"
