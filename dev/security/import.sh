#!/bin/bash
openssl x509 -outform der -in usgen-api-tw-otc.pem -out certificate.der
keytool -storepass changeit -import -alias taka -keystore $JAVA_HOME/lib/security/cacerts -file certificate.der
keytool -storepass changeit -keystore $JAVA_HOME/lib/security/cacerts -list | grep hello