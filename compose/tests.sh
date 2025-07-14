#!/bin/bash
set +ex

# Container setup
apt update
apt install -y curl jq
cp /debian-ssl/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt

fail=0

get_token() {
  local issuer=$1
  curl -u client-cred:secret "https://${issuer}.test.example/token" \
       -d grant_type=client_credentials -s | jq -r .access_token
}

access_apache() {
  local token=$1
  local url=$2

  sleep 5

  curl --connect-timeout 10 --max-time 30 -H "Authorization: Bearer $token" "$url" -L -s
}

validate_response() {
  local response=$1
  local expected=$2

  if [[ "$response" == "$expected" ]]; then
    echo "SUCCESS"
  else
    echo "FAIL: expected '$expected', got: $response"
    ((fail++))
  fi
}

# === Test 1 ===
echo -e "\nTest access to Apache trough access token from iam1"
BT=$(get_token iam1)
resp=$(access_apache "$BT" "https://apache.test.example/iam1")
validate_response "$resp" "Hello, this is /iam1"

# === Test 2 ===
echo -e "\nTest access to Apache trough access token from iam2"
BT=$(get_token iam2)
resp=$(access_apache "$BT" "https://apache.test.example/iam2")
validate_response "$resp" "Hello, this is /iam2"

# === Test 3 ===
echo -e "\nTest introspection endpoint with ESACO"
introspect=$(curl -u user:password https://esaco.test.example/introspect -d token=$BT -s)
if echo "$introspect" | jq .active | grep -q true; then
  echo "SUCCESS"
else
  echo "FAIL: token not active"
  ((fail++))
fi

echo -e "\nSummary:"
if [[ "$fail" -eq 0 ]]; then
  echo "All tests passed!"
else
  echo "$fail test(s) failed."
fi

exit $fail