import com.feiwoscun.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ConnectionPool connectionPool = new ConnectionPool();;
        while (true) {
            System.out.println("Hello world!");
            TimeUnit.SECONDS.sleep(1);
            Connection connection =
                    connectionPool.getConnection();
        }
    }
}