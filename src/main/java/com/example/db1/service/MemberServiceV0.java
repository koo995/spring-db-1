package com.example.db1.service;

import com.example.db1.domain.Member;
import com.example.db1.repository.MemberRepositoryV0;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션을 넣을 것이다. - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV0 {

    /**
     * 먼저 커넥션을 얻어야 하니까 dataSource 가 필요하다.
     */
    private final DataSource dataSource;

    private final MemberRepositoryV0 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false); // 트랜잭션 시작
            // 비즈니스 로직 수행
            bizLogic(fromId, toId, money, con);
            // 비즈니스 로직 종료
            con.commit(); // 커밋
        } catch (Exception e) {
            con.rollback(); // 실패시 롤백
            throw new IllegalStateException(e);
        } finally {
            /**
             * 커넥션을 릴리즈 해줘야 한다.
             * 그런데 커넥션 풀로 만약에 돌아간다면... 오토커밋이 false 라면 문제가 된다!
\             */
            release(con);
        }
    }

    private void bizLogic(String fromId, String toId, int money, Connection con) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private static void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); //
                con.close(); // 커넥션 풀을 사용한다면 이것이 릴리즈가 아니라 반납이 된다. 그렇게 세팅이 되어있다.
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}