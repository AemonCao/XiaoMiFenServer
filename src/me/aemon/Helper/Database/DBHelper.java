package me.aemon.Helper.Database;

import me.aemon.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 Aemon Cao
 * @创建时间 2018/12/3 21:46
 * @描述
 */
public class DBHelper {
    public static List<Map<String, Object>> queryAll(String strSql) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(Constants.JDBC_DRIVER);

            // 打开链接 连接数据库
            conn = DriverManager.getConnection(Constants.DB_URL, Constants.DB_USERNAME, Constants.DB_PASSWORD);

            // 执行查询 实例化Statement对象
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(strSql);
            ResultSetMetaData md = rs.getMetaData(); //获得结果集结构信息,元数据
            int columnCount = md.getColumnCount();   //获得列数
            // 展开结果集
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return list;
    }

    public static int uploadSql(String strSql) {
        Connection conn = null;
        Statement stmt = null;
        int count = 0;
        try {
            // 注册 JDBC 驱动
            Class.forName(Constants.JDBC_DRIVER);

            // 打开链接 连接数据库
            conn = DriverManager.getConnection(Constants.DB_URL, Constants.DB_USERNAME, Constants.DB_PASSWORD);

            // 执行查询 实例化Statement对象
            stmt = conn.createStatement();
            count = stmt.executeUpdate(strSql);
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return count;
    }
}
