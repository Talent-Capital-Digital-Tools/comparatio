#!/usr/bin/env bash
set -eu
if [ -f .env ]; then export $(grep -v '^#' .env | xargs); fi
exec java ${JAVA_OPTS:-} -jar target/comparatio-0.0.1-SNAPSHOT.jar


