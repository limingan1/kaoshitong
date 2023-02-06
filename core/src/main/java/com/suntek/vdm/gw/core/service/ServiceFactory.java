package com.suntek.vdm.gw.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.core.pojo.TableDate;

import java.util.List;
import java.util.Map;

public interface  ServiceFactory<T> extends BaseService{
        List<T> getListByLambda(LambdaQueryWrapper<T> lambdaQueryWrapper);
        T getOneByLambda(LambdaQueryWrapper<T> lambdaQueryWrapper);
        T getOneById(String id);
        List<T> getAll();
        boolean any(LambdaQueryWrapper<T> lambdaQueryWrapper);
        int count(LambdaQueryWrapper<T> lambdaQueryWrapper);
        TableDate getPage(Map<String, Object> query, Integer current, Integer size, String order, String orderType);
        BaseState add(T info);
        BaseState update(T info);
        BaseState del(String ids);

        }