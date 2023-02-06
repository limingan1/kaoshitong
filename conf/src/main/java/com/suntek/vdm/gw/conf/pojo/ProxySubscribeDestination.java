package com.suntek.vdm.gw.conf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 级联订阅路径 全系统唯一
 * 作为map的key使用必须重写hashCode和equals方法
 */
@Data
@AllArgsConstructor
public class ProxySubscribeDestination {
    private String token;
    private String destination;


    @Override
    public String toString(){
        return getSubscriptionId();
    }

    public String getSubscriptionId() {
        return  token+destination;
    }

    @Override
    public boolean equals(Object o) {

        if(this.hashCode() == o.hashCode()){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(destination);
        sb.append(token);
        char[] charArr = sb.toString().toCharArray();
        int hash = 0;
        for(char c : charArr) {
            hash = hash * 131 + c;
        }
        return hash;
    }
}
