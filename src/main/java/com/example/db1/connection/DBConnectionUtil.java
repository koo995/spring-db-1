package com.example.db1.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.example.db1.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    /**
     * 이 녀석이 jdbc 표준 인터페이스가 제공하는 커넥션이다.
     */
    public static Connection getConnection() {
        try {
            /**
             * 드라이브 매니저가 h2의 driver 을 찾는다. 그리고 그 드라이버를 통해서 실제 커넥션을 가져온다.
             * 잘 보면 가져온 Connection 은 인터페이스다. 뭔가 구현체가 나오겠지? 그 구현체가 뭐냐? h2의 Connection 이다. MySql 은 MySql 의 Connection 을 제공한다.
             * 둘다 jdbc 표준 connection 을 구현하고 있다. 그래서 jdbc 표준 인터페이스를 사용하면 db가 바뀌어도 코드를 변경할 필요가 없다.
             * org.h2.jdbc.JdbcConnection 을 반환한다. 이것이 h2의 Connection 구현체이다.
             * 그렇다면 이것을 어떻게 찾느냐? 드라이버 매니저가 찾아준다
             */
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            /**
             * checked exception 을 runtime exception 으로 변경하였다.
             */
            throw new IllegalStateException(e);
        }
    }

}
