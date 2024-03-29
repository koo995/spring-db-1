package com.example.db1.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.example.db1.connection.ConnectionConst.*;
@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        /**
         * 서로 다른 커넥션이다.
         */
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    /**
     * 기존의 DriverManager 은 DataSource  인터페이스를 사용하지 않는다.
     * 하지만 스프링은 DriverManager 도 DataSource 를 사용할 수 있도록
     * DriverManagerDataSource 라는 DataSource 구현체를 제공한다.
     * 이렇게 항상 새로운 커넥션을 획득한다.
     * 기존의 DriverManager 를 통해서 커넥션을 획득하는 방법과 DataSource 를 통해서 커넥션을 획득하는 방법에는 큰 차이가 있다.
     * DriverManagerDataSource 을 생성하는 시점에만 파라미터를 세팅해 주면 된다.
     * 그후 커넥션을 획득할때는 getConnection() 메소드만 호출하면 된다.
     * 설정과 사용의 분리다! 쉽게 이야기해서 리포지토리는 'DataSource' 만 의존하고, 설정값은 몰라도 된다.(한 곳에 잘 모아놔야 한다.)
     */
    @Test
    void dataSourceDriverManager() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

}
