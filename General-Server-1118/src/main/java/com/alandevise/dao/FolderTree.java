package com.alandevise.dao;

import com.alandevise.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @Filename: FolderTree.java
 * @Package: com.alandevise.dao
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月21日 22:04
 */

@Component
public interface FolderTree extends BaseMapper<com.alandevise.entity.FolderTree> {
    void insertAllInfo(@Param("id") String id,
                       @Param("treeId") String treeId,
                       @Param("treeParentId") String treeParentId,
                       @Param("name") String name,
                       @Param("type") Integer type,
                       @Param("path") String path);
}
