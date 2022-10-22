package com.alandevise;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

import java.io.File;

/**
 * @Filename: TraverseFolderTest.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年10月21日 21:36
 */

public class TraverseFolderTest {
    public static void main(String[] args) {
        String path = "D:/test";        // 要遍历的路径
        File file = new File(path);        // 获取其file对象
        TraverseAllFileInFolder(file);



        System.out.println(IdUtil.fastSimpleUUID());
    }

    private static void TraverseFileInFolder(String URL) {
        File file = new File(URL);        // 获取其file对象
        File[] fs = file.listFiles();    // 遍历path下的文件和目录，放在File数组中
        if (fs != null) {
            for (File f : fs) {                    // 遍历File[]数组
                if (!f.isDirectory())        // 若非目录(即文件)，则打印
                    System.out.println(f.getName() + " : " + f);
            }
        }
    }

    private static void TraverseAllFileInFolder(File file) {
        File[] fs = file.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory())    // 若是目录，则递归打印该目录下的文件
                    TraverseAllFileInFolder(f);
                if (f.isFile())        // 若是文件，直接打印
                    System.out.println(f);
            }
        }
    }
}
