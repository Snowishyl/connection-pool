package com.feiwoscun.pool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: feiWoSCun
 * @Time: 2024/11/13
 * @Email: 2825097536@qq.com
 * @description:
 */
public class ConnectionPool {
    /**
     * 配置文件默认地址
     */
    public static final String DB_PROPERTIES = "C:\\Users\\luobin\\IdeaProjects\\jdbc-connection-pool\\dbconfig\\db.properties";
    public static final int MIN_CONNECTIONS = 5;
    public static final int MAX_CONNECTIONS = 20;
    /**
     * 最大连接数
     */
    private final int maxConnections;
    /**
     * 最小连接数
     */
    private final int minConnections;
    /**
     * 定时任务
     */
    private final ScheduledExecutorService executorService;
    /**
     * 计数器，记录空闲连接数
     */
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    /**
     * 配置文件，为什么不放在{@link ConnectionFactory}中呢,对于工厂，应该所有的东西都应该是静态的，properties是动态的，
     * 一个项目可能会有多个properties，我们使用工厂只需要考虑生成Connection就行，配置文件的加载应该给每一个{@link ConnectionPool}对象
     */
    private final Properties properties;
    /**
     * 投入使用的连接
     */
    private final CopyOnWriteArrayList<ConnectionBean> activeConnections = new CopyOnWriteArrayList<>();
    /**
     * 空闲的连接数
     */
    private final CopyOnWriteArrayList<ConnectionBean> inactiveConnections = new CopyOnWriteArrayList<>();

    public ConnectionPool(int maxConnections, int minConnections, String fileUrl) {
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.properties = loadProperties(fileUrl);
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::check, 1, 10, TimeUnit.SECONDS);
    }

    public ConnectionPool(int maxConnections, int minConnections) {
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.properties = loadProperties();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(this::check);
    }

    public ConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
        this.minConnections = MIN_CONNECTIONS;
        this.properties = loadProperties();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(this::check);
    }

    public ConnectionPool() {
        this.maxConnections = MAX_CONNECTIONS;
        this.minConnections = MIN_CONNECTIONS;
        this.properties = loadProperties();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(this::check);
    }

    private void check() {
        checkInactiveNum();
        //inactive 检查
        checkInactive();
        //active检查
        checkActive();
    }

    private void checkInactiveNum() {
            while (inactiveConnections.size() < maxConnections >> 1) {
                Connection connection = ConnectionFactory.getConnection(properties);
                inactiveConnections.addLast(new ConnectionBean(connection, System.currentTimeMillis()));
                COUNTER.incrementAndGet();
            }
    }

    private void checkInactive() {
        for (int i = 0; i < inactiveConnections.size(); i++) {
            ConnectionBean connectionBean = inactiveConnections.get(i);
            Connection conn = connectionBean.getConn();
            try {
                if (conn == null || conn.isClosed()) {
                    inactiveConnections.remove(i);
                    COUNTER.decrementAndGet();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void checkActive() {
        for (int i = activeConnections.size(); i > 0; i--) {
            ConnectionBean activeConnection = activeConnections.get(i - 1);
            long createTime = activeConnection.getCreateTime();

            long currentTimeMillis = System.currentTimeMillis();

            long durable = currentTimeMillis - createTime;

            long activeTime = Long.parseLong(properties.getProperty("active-time"));
            if (durable < activeTime) {

                //do nothing
            } else {
                activeConnections.remove(i);
            }

        }
    }

    private void createConnections(int numConnections) {
        for (int i = 0; i < numConnections; i++) {
            Connection connection = ConnectionFactory.getConnection(properties);
            assert connection != null : "创建 Connection==null ! ";
            if (inactiveConnections.size() < maxConnections) {
                ConnectionBean connectionBean = new ConnectionBean(connection, System.currentTimeMillis());
                inactiveConnections.add(connectionBean);
                COUNTER.incrementAndGet();
            }
        }
        executorService.submit(this::check);
    }

    /**
     * 0的情况下创建一个放着就行
     *
     * @return
     */
    public Connection getConnection() {

        if (inactiveConnections.isEmpty()) {
            createConnections(1);
        }

        ConnectionBean first = inactiveConnections.removeFirst();
        activeConnections.addLast(first);
        COUNTER.decrementAndGet();
        return first.getConn();
    }

    private Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(fileName)) {
            properties.load(input);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("没有找到配置文件");
        } catch (IOException e) {
            throw new RuntimeException("发生io异常，转换成非受检异常");
        }
        return properties;
    }

    private Properties loadProperties() {
        return loadProperties(DB_PROPERTIES);
    }
}
