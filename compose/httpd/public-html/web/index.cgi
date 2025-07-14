#!/bin/bash

envvars=$(env)

cat << EOF

<html lang="en">
<head>
<meta charset="utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>Demo</title>
</head>
<body>
  <h1>DEMO WEB</h1>

  <p>You're now logged in as: <b>$OIDC_CLAIM_preferred_username</b></p>

  <p>This application has received the following information:</p>

  <ul>
    <li>access_token (JWT): <pre>$OIDC_access_token</pre></li>
    <li>access_token (decoded): <pre id="json"></pre></li>
<script>
document.getElementById("json").innerHTML = JSON.stringify($OIDC_access_token, undefined, 2);
</script>
  </ul>

  <a href="/web/redirect_uri?logout=https%3A%2F%2Fapache.test.example">Logout</a>
</body>
</html>

<pre>
$envvars
</pre>
EOF


