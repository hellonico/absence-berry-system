#!/bin/bash

export TZ=Asia/Tokyo ; export JAVA_OPTS="-DsocksProxyHost=localhost -DsocksProxyPort=10090" ;  lein ring server-headless