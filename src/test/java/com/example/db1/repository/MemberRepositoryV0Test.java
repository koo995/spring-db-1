package com.example.db1.repository;

import com.example.db1.domain.Member;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static com.example.db1.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository;

    @BeforeEach
    void beforeEach() {
        // 기본 DriverManagerDataSource 사용. 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        /**
         * 커넥션 풀링
         * 로그를 보면 모두 wrapping conn0 이다. 이것은 커넥션 풀링을 사용하고 있기 때문이다.
         * 쿼리가 끝나고 반환하고 끝나고 반환하고 순차적으로 돌기때문이다. 테스트는 순차적이기 때문.
         * 웹애플리케이션에서 멀티스레드로 동시요청하면 다른 커넥션을 쓸 것이다.
         * 단 히카리 객체의 주소는 다 다르다. 그러나 실제 커넥션은 같다. 히카리 프록시 객체 안에 실제 커넥션을 맵핑해서 반환하기때문. 객체 생성은 그다지 비용이 들지 않는다
         */
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        repository = new MemberRepositoryV0(dataSource);
    }

    @Test
    void crud() throws Exception {

        //save
        Member member = new Member("memberV0", 10000);
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        /**
         * member 객체에 @Data 어노테이션이 붙어있기 때문에 equals 메소드가 생성되어 있다.
         */
        assertThat(findMember).isEqualTo(member);

        // update
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // delete
        repository.delete(member.getMemberId());
        assertThrows(NoSuchElementException.class, () -> repository.findById(member.getMemberId()));

    }
}