package com.suntek.vdm.gw.core.service.impl;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.core.pojo.TableDate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ServiceFactoryImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 返回成功的业务信息(含消息)
     *
     * @param msg
     * @return 业务对象。
     */
    public BaseState success(String msg) {
        BaseState baseState = new BaseState();
        baseState.setCode(1);
        baseState.setMsg(msg);
        return baseState;
    }

    /**
     * 返回成功的业务信息(不含消息)
     *
     * @return 业务对象
     */
    public BaseState success() {
        BaseState baseState = new BaseState();
        baseState.setCode(1);
        baseState.setMsg("操作成功");
        return baseState;
    }

    /**
     * 返回失败的业务信息(含消息)
     *
     * @param msg
     * @return 业务对象
     */
    public BaseState fail(String msg) {
        BaseState baseState = new BaseState();
        baseState.setCode(0);
        baseState.setMsg(msg);
        return baseState;
    }

    /**
     * 返回失败的业务信息(不含消息)
     *
     * @return 业务对象
     */
    public BaseState fail() {
        BaseState baseState = new BaseState();
        baseState.setCode(0);
        baseState.setMsg("操作失败");
        return baseState;
    }

    /**
     * 根据状态返回业务消息
     *
     * @param i 数据行数
     * @return 业务对象
     */
    public BaseState state(boolean i) {
        if (i) {
            return success();
        } else {
            return fail();
        }
    }

    /**
     * 返回数据对象
     *
     * @param data  数据
     * @param count 记录条数
     * @return
     */
    public <T> TableDate toTableDate(List<T> data, Integer count) {
        TableDate<T> tTableDate = new TableDate<T>();
        tTableDate.setData(data);
        tTableDate.setCount(count);
        tTableDate.setMsg("数据获取成功");
        tTableDate.setCode(0);
        return tTableDate;
    }

    /**
     * Id转换
     *
     * @param ids
     * @return
     */
    public static List<String> idConvert(String ids) {
        List<String> removeId = new ArrayList<>();
        if (!ids.isEmpty()) {
            if (!ids.contains(",")) {
                removeId.add(ids);
            } else {
                for (String id : ids.split(",")) {
                    removeId.add(id);
                }
            }
        }
        return removeId;
    }


    /**
     * 获取TableField的值，取得真实的数据库字段
     *
     * @param t
     * @param name
     * @param <TT>
     * @return
     */
    public <TT> String getTableField(Class<T> t, String name) {
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);//设置发射时取消Java的访问检查，暴力访问
            if (field.getName().toLowerCase().equals(name.toLowerCase())) {
                // 获取字段的值
                boolean isTableField = field.isAnnotationPresent(TableField.class);
                if (isTableField) {
                    TableField tableField = field.getAnnotation(TableField.class);
                    return tableField.value();
                }
            }
        }
        return "id";
    }

    public <TT> void orderInit(QueryWrapper<TT> queryWrapper, String order, String orderType, Class<T> t) {
        if (order == null) {
            order = "id";
        }
        if (orderType == null) {
            orderType = "desc";
        }
        if ("asc".equals(orderType)) {
            queryWrapper.orderByAsc("id".equals(order) ? order : getTableField(t, order));
        } else {
            queryWrapper.orderByDesc("id".equals(order) ? order : getTableField(t, order));
        }
    }

    public <MM extends BaseMapper<TT>, TT> TableDate pageService(LambdaQueryWrapper<TT> lambdaQueryWrapper, int current, int size, MM mapper) {

        if (current == 0) {
            current = 1;
        }
        if (size == 0) {
            size = 20;
        }
        //分页
        IPage<TT> pageParameter = new Page(current, size);
        IPage page = mapper.selectPage(pageParameter, lambdaQueryWrapper);
        List<TT> data = page.getRecords();
        int count = mapper.selectCount(lambdaQueryWrapper).intValue();
        return toTableDate(data, count);
    }

    public TableDate pageService(LambdaQueryWrapper<T> lambdaQueryWrapper, Integer current, Integer size) {
        if (current == null || current == 0) {
            current = 1;
        }
        if (size == null || size == 0) {
            size = 20;
        }
        //分页
        IPage<T> pageParameter = new Page(current, size);
        IPage page = page(pageParameter, lambdaQueryWrapper);
        List<T> data = page.getRecords();
        int count = (int) page.getTotal();
        return toTableDate(data, count);
    }


    //endregion


    //region ServiceFactory 接口实现方法
    public List<T> getAll() {
        return super.list();
    }

    public T getOneById(String id) {
        return getById(id);
    }

    public List<T> getListByLambda(LambdaQueryWrapper<T> lambdaQueryWrapper) {
        return super.list(lambdaQueryWrapper);
    }

    public T getOneByLambda(LambdaQueryWrapper<T> lambdaQueryWrapper) {
        return super.getOne(lambdaQueryWrapper);
    }

    public BaseState del(String ids) {
        return state(super.removeByIds(idConvert(ids)));
    }

    public boolean any(LambdaQueryWrapper<T> lambdaQueryWrapper) {
        return super.count(lambdaQueryWrapper) > 0;
    }

    public int count(LambdaQueryWrapper<T> lambdaQueryWrapper) {
        return (int) super.count(lambdaQueryWrapper);
    }
    //endregion
}
