#!/usr/bin/env bash
set -e

#################################################################################
######################          脚本参数说明               ########################
#   1.支持编译Enterprise Professional Smart Player UGC等SDK
#   2.可通过外部变量SDK控制需要编译哪些SDK，如RDM的编译参数（本地环境变量）设置SDK=Enterprise
#      或者SDK=ALL，ALL表示全量编译，其余表示编译SDK组合
#   3.每次SDK构建默认编译SDK(包括aar和jar+so),apk及打包对应demo源码
#   4.编译Enterprise的SDK时编译小直播apk，ALL选项同时打包小直播源码
#   5.编译sdk或者apk失败会终止编译，清理编译中间结果后退出
#   6.编译参数：PUBLISH_VERSION=YES表示非测试版本，需要去掉测试代码，DEMO=YES表示不编译小直播，小视频
#   7.apk 命名规则应符合正则：name_v0.1.xx.apk 、 name_0.2.xx.apk 、 name_V0.1.xx.apk ，这样强制升级后台才能识别；
############################################### ##################################

#设置RDM环境变量
export ANDROID_HOME=$ANDROID_SDK
export JAVA_HOME=$JDK8
export GRADLE_HOME=/data/rdm/apps/gradle/gradle-3.1
export PATH=$JDK8/bin:$GRADLE_HOME/bin:$PATH
export ANDROID_NDK_HOME=$ANDROIDNDK_LINUX_R12B
export GRADLE_USER_HOME=$WORKSPACE
export LANG="zh_CN.UTF-8"
export LC_ALL="zh_CN.UTF-8"

#################################################################################
######################          构建辅助函数               ########################
#################################################################################

buildLog() {
    echo `date +"%Y-%m-%d %H:%M:%S"`" build process: $1"
}

getSDKVersion() {
    files=$(ls ${SDK_PATH})
    for filename in ${files}
    do
      if [[ "${filename}" =~ LiteAVSDK_$1_ ]]; then
          SDK_VERSION=$(echo "${filename}" | tr -cd "^[0-9\.]+$")
          SDK_VERSION=${SDK_VERSION%?}
      fi
    done
    buildLog "$1 getSDKVersion $SDK_VERSION"
}

getImZipFile() {
    files=$(ls ${SDK_PATH})
    for file in ${files}
    do
      if [[ "${file}" =~ imsdk-plus- ]]; then
          echo "getImZipFile file ：$file"
          IM_SDK_ZIP=${file}
      fi
    done
    buildLog "getImZipFile $IM_SDK_ZIP"
}

getIMSDKVersion() {
    files=$(ls ${SDK_PATH})
    for filename in ${files}
    do
      if [[ "${filename}" =~ imsdk-plus- ]]; then
          IM_SDK_VERSION=$(echo "${filename}" | tr -cd "^[0-9\.]+$")
          IM_SDK_VERSION=${IM_SDK_VERSION%?}
      fi
    done
    buildLog "getIMSDKVersion $IM_SDK_VERSION"
}

getRoomEngineVersion() {
    ROOM_ENGINE_VERSION=""
    files=$(ls ${SDK_PATH})
    for filename in ${files}
    do
      if [[ "${filename}" =~ rtc_room_engine_ ]]; then
          ROOM_ENGINE_VERSION=$(echo "${filename}" | tr -cd "^[0-9\.]+$")
          ROOM_ENGINE_VERSION=${ROOM_ENGINE_VERSION%?}
      fi
    done
    buildLog "getRoomEngineVersion $ROOM_ENGINE_VERSION"
}

fetchAarFromZip() {
    cd ${SDK_PATH}

    unzip -q ${SDK_PATH}/LiteAVSDK_$1_Android_${SDK_VERSION}.zip
    cp ${SDK_PATH}/LiteAVSDK_$1_Android_${SDK_VERSION}/SDK/LiteAVSDK_$1_${SDK_VERSION}.aar ${SDK_PATH}/LiteAVSDK_$1_${SDK_VERSION}.aar
    cp ${SDK_PATH}/LiteAVSDK_$1_Android_${SDK_VERSION}/SDK/LiteAVSDK_$1_${SDK_VERSION}.zip ${SDK_PATH}/LiteAVSDK_$1_${SDK_VERSION}.zip
    rm -rf ${SDK_PATH}/LiteAVSDK_$1_Android_${SDK_VERSION}.zip
    rm -rf ${SDK_PATH}/LiteAVSDK_$1_Android_${SDK_VERSION}/SDK

    pwd
    echo "ls $1 aar start"
    ls ${SDK_PATH}
    echo "ls $1 aar end"

    cd ${WORKSPACE}
}

printObviousLog() {
    echo "
|================================================================================|
|--------------------------------------------------------------------------------|
|                                                                                |
    #######  $1  #######
|                                                                                |
|--------------------------------------------------------------------------------|
|================================================================================|"
}

printCostTime() {
    et=$(date +"%Y-%m-%d %H:%M:%S")
    endTime=$(date -d  "$et" +%s)
    startTime=$1
    sumTime=$(($endTime-$startTime))
    printObviousLog "$2 cost time is : $sumTime second"
}

checkBuildResult() {
    if [[ $? -ne 0 ]]; then
        info="build $1 failed !!! "
        echo -e "\033[31m${info}\033[0m"
        exit 1
    else
        buildLog "build $1 sucess !!!"
    fi
}

buildTUIRoomKitApk() {
 #   fetchAarFromZip "TRTC"
    #创建编译工程
    RTCUBE_OUTPUT_DIR=${RDM_OUTPUT}/LiteAVSDK_RTCube_Android_${SDK_VERSION}
    mkdir -p "${RTCUBE_OUTPUT_DIR}"
    RTCUBE_PRJ=${WORKSPACE}/tui-components/Android/TUIRoomKit

    buildLog "start buildRTCubeAPK ${SDK_Name}"

    mkdir -p ${RTCUBE_PRJ}/tuiroomkit/libs
    rm -rf ${RTCUBE_PRJ}/tuiroomkit/libs/*
    cp ${SDK_PATH}/rtc_room_engine_${ROOM_ENGINE_VERSION}.aar ${RTCUBE_PRJ}/tuiroomkit/libs/rtc_room_engine_${ROOM_ENGINE_VERSION}.aar
    sed -i "/io.trtc.uikit:rtc_room_engine/d" ${RTCUBE_PRJ}/tuiroomkit/build.gradle
    echo "current landun roomengine sdk"
    ls ${RTCUBE_PRJ}/tuiroomkit/libs

    # 替换 IM 蓝盾流水线的 SDK
    getImZipFile
    FILE_IM=${IM_SDK_ZIP}
    echo "buildTUIRoomKitApk landun IM zip : ${FILE_IM}"
    if [ -f "${SDK_PATH}/$FILE_IM" ]; then
        unzip -q -o -d ${SDK_PATH} ${SDK_PATH}/${FILE_IM}
        getIMSDKVersion
        echo "buildTUIRoomKitApk landun IM sdk : ${IM_SDK_VERSION}"
        mkdir -p ${RTCUBE_PRJ}/app/libs
        cp ${SDK_PATH}/imsdk-plus-${IM_SDK_VERSION}.aar ${RTCUBE_PRJ}/app/libs/imsdk-plus-${IM_SDK_VERSION}.aar
        sed -i "s/com.tencent.imsdk.*/com.tencent.imsdk:imsdk-plus:${IM_SDK_VERSION}@aar\"/" ${RTCUBE_PRJ}/build.gradle
        ls ${RTCUBE_PRJ}/app/libs
    fi

    sed -i 's/1.0.0/'${APP_VERSION}'/' ${RTCUBE_PRJ}/app/build.gradle
    sed -i 's/versionCode 1/versionCode '${VERSION_CODE}'/' ${RTCUBE_PRJ}/app/build.gradle

    sed -i 's/SDKAppID = PLACEHOLDER/SDKAppID = 1400704311/' ${RTCUBE_PRJ}/debug/src/main/java/com/tencent/liteav/debug/GenerateTestUserSig.java
    sed -i 's/SDKSecretKey = "PLACEHOLDER"/SDKSecretKey = "8b897045d1ee4f067a745b1b6a3fb834d1bd4c5951de43282c21b945f98ec982"/' ${RTCUBE_PRJ}/debug/src/main/java/com/tencent/liteav/debug/GenerateTestUserSig.java

    # 删除.git
    rm -rf "${RTCUBE_PRJ}"/.git
    gradle clean -p ${RTCUBE_PRJ}
    if [[ "RELEASE" == ${APP_MODE} ]]; then
        gradle app:assembleRelease -x lint -p "${RTCUBE_PRJ}"
        cp "${RTCUBE_PRJ}"/app/build/outputs/apk/release/app-release*apk "${RDM_OUTPUT}"/TUIRoomKit_Android_Release_"${ROOM_ENGINE_VERSION}"_"${IM_SDK_VERSION}".apk
    else
        gradle app:assembleDebug -x lint -p ${RTCUBE_PRJ}
        cp "${RTCUBE_PRJ}"/app/build/outputs/apk/debug/app-debug.apk "${RDM_OUTPUT}"/TUIRoomKit_Android_Debug_"${ROOM_ENGINE_VERSION}"_"${IM_SDK_VERSION}".apk
    fi

    # 删除build目录
    gradle clean -p ${RTCUBE_PRJ}
}


#################################################################################
######################          RDM构建流程               #########################
#################################################################################

#设置编译过程用到的变量
SHELL_DIR=$(cd `dirname $0`; pwd)
RDM_OUTPUT=${WORKSPACE}/bin
rm -rf ${RDM_OUTPUT}
mkdir -p ${RDM_OUTPUT}

# APP 的版本号
APP_VERSION=${MajorVersion}.${MinorVersion}.${FixVersion}.${BuildNo}

# 用于存放下载后的 SDK zip 包，zip 由蓝盾插件下载并保存在这路径下
SDK_PATH=${WORKSPACE}/SDK
#getSDKVersion "TRTC"
getRoomEngineVersion
IM_SDK_ZIP="IM_SDK_ZIP"
IM_SDK_VERSION="maven"

TUICOMONENTS_PATH=${WORKSPACE}/tui-components/Android
#app模式为debug时，去除登陆界面，其他需要保留登陆界面
buildLog "APP_VERSION: ${APP_VERSION}, SDK_VERSION: ${SDK_VERSION}, APP_MODE is : ${APP_MODE}"

cd ${SHELL_DIR}

printObviousLog "start build RT-Cube APP"
curTime=`date +"%Y-%m-%d %H:%M:%S"`
startTime=`date -d  "$curTime" +%s`
buildTUIRoomKitApk
printCostTime $startTime "build RT-Cube AP"