#!/usr/bin/env bash

#################################################################################
######################          脚本说明               ########################
#   蓝盾构建出来的apk，通过腾讯证书签名后，会保留签名前后的apk，本脚本用来删除未签名apk，并将签名apk重命名成签名前的apk，因为名称变了会导致后续脚本识别不了对应apk。
#   举个例子：
#   1、蓝盾构建出 demo.apk；
#   2、通过腾讯证书签名后，会生成 demo.apk 和 demo-signed.apk；
#   3、本脚本删除 demo.apk；
#   4、本脚本将 demo-signed.apk 重命名成 demo.apk。
#################################################################################

#设置RDM环境变量
export ANDROID_HOME=$ANDROID_SDK
export JAVA_HOME=$JDK8
export GRADLE_HOME=/data/rdm/apps/gradle/gradle-3.1
export PATH=$JDK8/bin:$GRADLE_HOME/bin:$PATH
export ANDROID_NDK_HOME=$ANDROIDNDK_LINUX_R12B
export GRADLE_USER_HOME=$WORKSPACE
export LANG="zh_CN.UTF-8"
export LC_ALL="zh_CN.UTF-8"

buildLog() {
    echo `date +"%Y-%m-%d %H:%M:%S"`" build process : $1"
}

APK_DIR=${WORKSPACE}/bin

for file in $(ls ${APK_DIR} | grep "signed.apk")
do
    buildLog $file
    prefix=${file%-signed*}
    unsignedName=${prefix}.apk
    buildLog $unsignedName
    rm  -f ${APK_DIR}/${unsignedName}
    mv ${APK_DIR}/${file} ${APK_DIR}/${unsignedName}
done
