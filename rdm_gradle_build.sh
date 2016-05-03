#!/bin/bash
export ANDROID_HOME=$ANDROIDSDK_LINUX_R22
export JAVA_HOME=$JDK7
export PATH=$JDK7/bin:$GRADLE_HOME/bin:$PATH
pushd app/src/main/jniLibs
unzip armeabi.zip
popd
gradle clean
gradle build
if [ $? -ne 0 ]; then
    echo "failed compiled!"
    exit 1
fi
cp app/build/outputs/apk/*.apk bin
