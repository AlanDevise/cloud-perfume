package com.alandevise.schedule;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @Filename: InsertData.java
 * @Package: com.alandevise.schedule
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月22日 12:20
 */

@Slf4j
// @Component
public class InsertData {

    @Autowired
    FolderTree folderTree;

    //此处为间隔5秒
    @Scheduled(fixedDelay = 5000)
    public void action() throws InterruptedException {
        folderTree.insertAllInfo(IdUtil.fastSimpleUUID(),
                IdUtil.fastSimpleUUID(),
                IdUtil.fastSimpleUUID(),
                "asdf",
                0,
                "/asdfasdfa");
    }

}

