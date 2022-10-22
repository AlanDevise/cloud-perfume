package com.alandevise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Filename: FolderTree.java
 * @Package: com.alandevise.entity
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月21日 22:17
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderTree {
    String id;
    String treeId;
    String treeParentId;
    String name;
    Integer type;
    String path;
}
