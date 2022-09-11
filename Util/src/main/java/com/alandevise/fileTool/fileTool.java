package com.alandevise.fileTool;

import com.oracle.tools.packager.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @Filename: fileTool.java
 * @Package: com.alandevise
 * @Version: V1.0.0
 * @Description: 1. 针对文件的处理方法集合
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月11日 13:43
 */

@Slf4j
public class fileTool {
    /*
     * 将from指向的文件拷贝至to指向的路径，若to指向的不存在则新建，若存在则覆盖
     * */
    public static void CopyFileViaChannel(FileChannel from, FileChannel to) {
        try {
            from.transferTo(0, from.size(), to);    // 效率高，底层会利用操作系统的零拷贝进行优化
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 将SourceURI指向的文件拷贝至TargetURI指向的文件，若目标路径存在同名文件会报FileAlreadyExistsException 异常
     * */
    public static void CopyFileViaFiles(String SourceURI, String TargetURI) {
        Path source = Paths.get(SourceURI);
        Path target = Paths.get(TargetURI);
        try {
            Files.copy(source, target);
        } catch (FileAlreadyExistsException exception) {
            Log.info("[EXCEPTION] 文件或路径已存在");
        } catch (IOException exception) {
            Log.info("[EXCEPTION] 拷贝文件失败");
        }
    }

    /*
     * 将SourceURI指向的文件强制拷贝至TargetURI指向的文件
     * */
    public static void CopyFileViaFilesForcely(String SourceURI, String TargetURI) {
        Path source = Paths.get(SourceURI);
        Path target = Paths.get(TargetURI);
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            Log.info("[EXCEPTION] 拷贝文件失败");
        }
    }

    /*
     * 判断路径是否存在，或者文件是否存在
     * */
    public static Boolean isExists(String uri) {
        Path path = Paths.get(uri);
        return Files.exists(path);
    }

    /*
     * 移动文件，从指定路径移动到目标路径
     * */
    public static void MoveFile(String SourceURI, String TargetURI) {
        Path source = Paths.get(SourceURI);
        Path target = Paths.get(TargetURI);
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            Log.info("[EXCEPTION] 移动文件失败");
        }
    }

    /*
     * 删除文件，或文件夹路径
     * */
    public static void DeleteFile(String FileURI) {
        Path target = Paths.get(FileURI);
        try {
            Files.delete(target);
        } catch (DirectoryNotEmptyException exception) {
            Log.info("[EXCEPTION] 该路径不为空，无法删除");
        } catch (NoSuchFileException exception) {
            Log.info("[EXCEPTION] 该路径下文件不存在");
        } catch (IOException exception) {
            Log.info("[EXCEPTION] 删除文件操作异常");
        }
    }

    /*
    * [WARNING] 极为危险的操作，逐级删除路径下所有的文件及文件夹，且不可恢复，慎用
    * */
    private static void DeleteVariousFiles(String TargetURI) throws IOException {
        Files.walkFileTree(Paths.get(TargetURI), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                Log.info(file+" 已被删除");
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                Log.info(dir+" 已被删除");
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
