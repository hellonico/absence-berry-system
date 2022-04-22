#!/bin/bash
openssl x509 -outform der -in usgen-api-tw-otc.pem -out certificate.der
keytool -import -alias hello -keystore $JAVA_HOME/lib/security/cacerts certificate.der