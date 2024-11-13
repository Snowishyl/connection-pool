package com.feiwoscun.pool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * @author: feiWoSCun
 * @Time: 2024/11/13
 * @Email: 2825097536@qq.com
 * @description:
 */
public class ConnectionFactory {
    private static final int INITIAL_NUM = 10;


    public static Connection getConnection(Properties properties) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            String url = properties.getProperty("jdbc.url");
            String username = properties.getProperty("jdbc.username");
            String password = properties.getProperty("jdbc.password");
            String driver = properties.getProperty("jdbc.driver");

            // 加载并注册数据库驱动
            Class.forName(driver);

            // 建立数据库连接
            connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException | SQLException ignored) {

        }
        return connection;
    }


}
