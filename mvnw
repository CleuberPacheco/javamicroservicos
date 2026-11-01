#!/bin/sh
# Maven Wrapper script for Unix/Linux/macOS
MAVEN_VERSION="3.9.9"
MAVEN_DIR="$HOME/.m2/wrapper/dists/apache-maven-$MAVEN_VERSION"
MVN_BIN="$MAVEN_DIR/apache-maven-$MAVEN_VERSION/bin/mvn"

if [ ! -x "$MVN_BIN" ]; then
  mkdir -p "$MAVEN_DIR"
  curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz" | tar -xz -C "$MAVEN_DIR"
fi

exec "$MVN_BIN" "$@"
