package com.feiwoscun.pool;

import java.sql.Connection;

/**
 * @author: feiWoSCun
 * @Time: 2024/11/13
 * @Email: 2825097536@qq.com
 * @description: 用来保存连接信息的最小单位
 */
public class ConnectionBean {


    private Connection conn;
    private long createTime;

    public ConnectionBean(Connection conn, long createTime) {
        this.conn = conn;
        this.createTime = createTime;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
