package com.lancer.utils;

import com.lancer.entity.LancerMes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    public static LancerMes checkPath(String pathString){
        LancerMes lancerMes = new LancerMes();
        // 检查路径格式是否合法
        Path path = Paths.get(pathString);
        if (!path.isAbsolute()) {
            System.out.println("路径不合法，请提供合法的路径。");
            lancerMes.setIsOk(false);
            return lancerMes;
        }
        // 检查路径是否存在
        if (!Files.exists(path)) {
            System.out.println("路径不存在，尝试创建...");
            try {
                // 创建路径，包括所有不存在的父目录
                Files.createDirectories(path);
                lancerMes.setIsOk(true);
                lancerMes.setMes(pathString+"/");
                System.out.println("路径已成功创建。");
                return lancerMes;
            } catch (IOException e) {
                System.out.println("创建路径失败：" + e.getMessage());
                lancerMes.setIsOk(false);
                return lancerMes;
            }
        } else {
            File testFile = new File(pathString, "test_permission.tmp");

            try {
                // 尝试创建文件
                if (testFile.createNewFile()) {
                    // 删除临时文件
                    testFile.delete();
                }
            } catch (IOException e) {
                System.err.println("当前路径没有写入权限，请更换路径： " + e.getMessage());
                lancerMes.setIsOk(false);
                return lancerMes;
            }
            lancerMes.setIsOk(true);
            lancerMes.setMes(pathString);
            return lancerMes;
        }
    }
}
