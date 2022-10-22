package com.alandevise.controller;

import cn.hutool.core.util.IdUtil;
import com.alandevise.dao.FolderTree;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.Stack;

/**
 * @Filename: MySQLTest.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-22 11:23
 */

@RestController
@RequestMapping("/general")
@Api(tags = "MySQL测试接口", value = "MySQL测试接口")
@Slf4j
public class MySQLTest {

    @Autowired
    private FolderTree folderTree;

    // 创建一个堆栈来存储父节点ID，符合FILO
    static Stack<String> parentId = new Stack<>();

    @GetMapping("/getTest")
    @ApiOperation("MySQL基本操作测试-GET")
    public String FirstTest() {
        return "运行到了Controller层";
    }

    @GetMapping("/FolderTree")
    @ApiOperation("遍历文件夹测试-GET")
    public void FolderTree() {
        // 根目录文件
        String rootPath = "D:/test";
        File rootFile = new File(rootPath);
        TraverseAllFileInFolder(rootFile);
    }

    private void TraverseAllFileInFolder(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {   // 是目录
                    String id = IdUtil.fastSimpleUUID();
                    String treeId = IdUtil.fastSimpleUUID();
                    String treeParentId;
                    if (!parentId.isEmpty()) {
                        treeParentId = parentId.peek();
                    } else {
                        treeParentId = "";
                    }
                    String name = f.getName();
                    Integer type = 0;
                    folderTree.insertAllInfo(id, treeId, treeParentId, name, type, ""); // 更新插入到数据库
                    parentId.push(treeId);  // 将当前树形节点压入栈
                    TraverseAllFileInFolder(f);
                }
                if (f.isFile()) {    // 是文件
                    String id = IdUtil.fastSimpleUUID();
                    String treeId = IdUtil.fastSimpleUUID();
                    String treeParentId;
                    if (!parentId.isEmpty()) {
                        treeParentId = parentId.peek(); // 取最上层的数据
                    } else {
                        treeParentId = "";
                    }
                    String name = f.getName();
                    Integer type = 1;
                    folderTree.insertAllInfo(id, treeId, treeParentId, name, type, ((f.getPath()).replaceAll("\\\\","/")).replaceAll("D:/","")); // 更新插入到数据库
                }
            }
            if (!parentId.isEmpty()) {
                parentId.pop();     // 一整个文件夹遍历完及离开出栈
            }
        }
    }
}
