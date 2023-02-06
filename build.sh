#!/bin/bash
cd ../../cti_vdm_mixServer
read -p "packet liceese?(Y/N): " input
case $input in
    "Y"|"y")
        echo "打包license..."
        git reset --hard
        git pull --rebase
        git log -1
        rm -rf vdm-license/target/vdm-license-*.jar
        rm -rf vdm-license/pom.xml
        mv ../cti_vdm_cascade/cascadegwr3.0/windows/tools/pom.xml vdm-license/pom.xml
        mvn clean install -pl vdm-license -am dependency:copy-dependencies -DoutputDirectory=target/lib
        echo "打包license完成"
      ;;
esac

cd ../cti_vdm_cascade/cascadegwr3.0

rootDir=/home/vdc
casLibDir=${rootDir}/cascadegwr/lib
echo =================================
echo  'Maven脚本启动'
echo =================================

echo '开始打包'
mvn clean package

echo '复制'
cp -r target/cascadegwr3.0.jar  target
cp -r suse/*  target

echo '授权'
dos2unix target/tools/*.sh
chmod +x target/tools/*.sh

echo '处理完成'