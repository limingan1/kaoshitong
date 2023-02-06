#!/bin/bash
tools_path=/home/vdc/base/tools
function start(){
  cd /home/vdc/cascadegwr/3.0
  nohup /home/vdc/base/jdk/bin/java -Dlog4j2.formatMsgNoLookups=true -jar /home/vdc/cascadegwr/3.0/cascadegwr3.0.jar >/dev/null 2>&1 &
  bindCpu
}

function bindCpu(){
  if [[ -f ${tools_path}/VdcConfig.cfg ]]; then
         cpus=$(cat ${tools_path}/VdcConfig.cfg | awk -F ':' '{print $2}')
         pid=$(ps -ef | grep java |grep cascadegwr3.0.jar |grep -v grep |awk '{print $2}')
         if [[ "x${pid}" != "x" && "x${cpus}" != "x" ]];then
             taskset -cp ${cpus} ${pid}
         fi
  fi
}
start
