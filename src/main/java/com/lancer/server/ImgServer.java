package com.lancer.server;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import com.lancer.entity.TableFiled;
import com.lancer.entity.TableInfo;
import org.assertj.core.util.Lists;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ImgServer {
    public static String out_path = "X:\\";
    public static String dbHost = "127.0.0.1";
    public static int dbPort = 3306;
    public static String dbName = "books";
    public static String userName = "root";

    public static String password = "";

    // 需要排除的字段
    final static List<String> excludeFiledArr = Arrays.asList("create_time", "update_time", "create_user", "update_user", "delete_flag");


    /**
     * 字段排除
     *
     * @param getTableFields 获取表字段
     * @return {@link List}<{@link TableFiled}>
     */
    public static List<TableFiled> excludeFiled(List<TableFiled> getTableFields){
        List<TableFiled> fileds = new ArrayList<>();

        for (TableFiled filed : getTableFields) {
            String lowerCase = filed.getField().toLowerCase();
            if (!excludeFiledArr.contains(lowerCase)) {
                fileds.add(filed);
            }
        }
        return fileds;
    }

    public static List<TableFiled> getTableFields(DataSource ds, String tblName) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<TableFiled> list = Lists.newArrayList();
        try {
            conn = ds.getConnection();
            String sql = "SHOW FULL FIELDS FROM " + tblName;
            stmt = conn.prepareStatement(sql);
            // 1~9 Field,Type,Collation,Null,Key,Default,Extra,Privileges,Comment
            rs = stmt.executeQuery();
            while (rs.next()) {
                TableFiled field = new TableFiled();
                field.setField(rs.getString(1));
                field.setComment(rs.getString(9));
                list.add(field);
            }
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
            JdbcUtils.close(conn);
        }
        return list;
    }

    public static void showAllDbName(DataSource ds) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库元数据
            DatabaseMetaData metaData =  ds.getConnection().getMetaData();

            // 获取所有数据库名称
            ResultSet resultSet = metaData.getCatalogs();
            List<String> databaseNames = new ArrayList<>();
            while (resultSet.next()) {
                String databaseName = resultSet.getString("TABLE_CAT");
                // 排除MySQL自带的系统数据库
                if (!"information_schema".equals(databaseName) &&
                        !"mysql".equals(databaseName) &&
                        !"performance_schema".equals(databaseName) &&
                        !"sys".equals(databaseName)) {
                    databaseNames.add(databaseName);
                }
            }
            resultSet.close();
            System.out.println("所有的数据库如下：\n");
            int count = 0;
            for (String dbName : databaseNames) {
                System.out.print(dbName + "\t");
                count++;
                if (count % 4 == 0) {
                    System.out.println();
                }
            }
            System.out.println("\n");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
            JdbcUtils.close(conn);
        }
    }
    public static List<TableInfo> getTableInfos(DataSource ds, String databaseName) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<TableInfo> list = Lists.newArrayList();
        try {
            conn = ds.getConnection();
            String sql = "select TABLE_NAME,TABLE_COMMENT from information_schema.tables where table_schema =? order by table_name";

            stmt = conn.prepareStatement(sql);
            setParameters(stmt, Collections.singletonList(databaseName));

            rs = stmt.executeQuery();
            while (rs.next()) {
                TableInfo row = new TableInfo();
                row.setTblName(rs.getString(1));
                row.setTblComment(rs.getString(2));
                list.add(row);
            }
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
            JdbcUtils.close(conn);
        }
        return list;
    }


    private static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0, size = parameters.size(); i < size; ++i) {
            Object param = parameters.get(i);
            stmt.setObject(i + 1, param);
        }
    }

    public static DataSource getDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&useInformationSchema=true");
        datasource.setUsername(userName);
        datasource.setPassword(password);
        datasource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        datasource.setInitialSize(1);
        datasource.setMinIdle(1);
        datasource.setMaxActive(3);
        datasource.setMaxWait(60000);
        return datasource;
    }
}
