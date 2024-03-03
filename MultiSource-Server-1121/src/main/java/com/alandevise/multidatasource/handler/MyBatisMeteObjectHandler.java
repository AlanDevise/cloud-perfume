package com.alandevise.multidatasource.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @Filename: MyBatisMeteObjectHandler.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
public class MyBatisMeteObjectHandler implements MetaObjectHandler {


    /**
     * insert操作时要填充的字段
     * 使用示例: @TableField(fill = FieldFill.INSERT)
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 根据属性名字设置要填充的值
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
    }

    /**
     * update操作时要填充的字段
     * 使用示例: @TableField(fill = FieldFill.INSERT_UPDATE)
     * 注意事项: 无法获取到泛型AlgoHosts, 自动填充将失效
     * hostsService.update(new UpdateWrapper<AlgoHosts>().eq("id",6).set("algorithm_config",""));
     * 解决方法:
     * hostsService.update(new AlgoHosts(),new UpdateWrapper<AlgoHosts>().eq("id",6).set("algorithm_config",""));
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());
    }

    /**
     * 覆盖父类的严格模式填充策略，即使有值也将其覆盖
     *
     * @param metaObject metaObject meta object parameter
     * @param fieldName  java bean property name
     * @param fieldVal   java bean property value of Supplier
     * @return this
     */
    @Override
    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
        Object obj = fieldVal.get();
        if (Objects.nonNull(obj)) {
            metaObject.setValue(fieldName, obj);
        }
        return this;
    }


}
