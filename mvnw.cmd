@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

@echo off
setlocal

set WRAPPER_DIR=%~dp0
set MAVEN_USER_HOME=%USERPROFILE%\.m2
set MAVEN_VERSION=3.9.9
set MAVEN_DIR=%MAVEN_USER_HOME%\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MVN_CMD=%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd

if not exist "%MVN_CMD%" (
    echo Baixando Apache Maven %MAVEN_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ErrorActionPreference = 'Stop'; " ^
        "$zipPath = '%MAVEN_DIR%\maven.zip'; " ^
        "New-Item -ItemType Directory -Force -Path '%MAVEN_DIR%' | Out-Null; " ^
        "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile $zipPath; " ^
        "Expand-Archive -Path $zipPath -DestinationPath '%MAVEN_DIR%' -Force; " ^
        "Remove-Item $zipPath -Force"
)

call "%MVN_CMD%" %*
endlocal
