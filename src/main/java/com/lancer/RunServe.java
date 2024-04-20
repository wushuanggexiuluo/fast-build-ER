package com.lancer;

import com.lancer.entity.LancerMes;
import com.lancer.entity.TableFiled;
import com.lancer.entity.TableInfo;
import com.lancer.server.DrawServer;
import com.lancer.server.ImgServer;
import com.lancer.utils.PathUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Scanner;

@Component
public class RunServe implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("欢迎使用一键生成ER图程序 \n 注意：\n 1 默认的用户名是root,如果更换请在ImgServer的userName中替换 \n 2 为了美观，在使用时要求库中的表、字段有注释，注释的长度不要超过12个中文字符；表中的字段数不要超过14个 \n --请输入生成路径（路径示例：D://abc/a）\n");
        String path = scanner.nextLine();
        LancerMes res = PathUtils.checkPath(path);
        if (!res.getIsOk()) {
            return;
        }
        ImgServer.out_path = res.getMes();

        System.out.print("--请数据库输入密码\n");
        try {
            ImgServer.password = scanner.nextLine();

        } catch (Exception e) {
            System.out.println("数据库密码不正确。");
        }
        DataSource source = ImgServer.getDataSource();
        ImgServer.showAllDbName(source);

        System.out.print("--请输入数据库名\n");
        ImgServer.dbName = scanner.nextLine();
        try {
            DataSource ds = ImgServer.getDataSource();
            // 1 获取数据表的信息
            List<TableInfo> tables = ImgServer.getTableInfos(ds, ImgServer.dbName);
            // 2 绘制图表并保存
            for (TableInfo table : tables) {
                // 获取当前数据表的所有字段信息
                List<TableFiled> fields = ImgServer.getTableFields(ds, table.getTblName());
                // 排除不用的字段
                List<TableFiled> tableFileds = ImgServer.excludeFiled(fields);
                // 构建图并保存
                DrawServer.buildChart(table, tableFileds, ImgServer.out_path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            scanner.close();
            System.out.println("生成的图片已经保存在：" + ImgServer.out_path + "路径下");
        }
    }

}
