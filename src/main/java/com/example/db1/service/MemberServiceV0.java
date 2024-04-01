package com.example.db1.service;

import com.example.db1.domain.Member;
import com.example.db1.repository.MemberRepositoryV0;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

/**
 * 트랜잭션을 넣을 것이다. - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV0 {

    /**
     * 먼저 커넥션을 얻어야 하니까 dataSource 가 필요하다.
     * 현재 문제가 되는 것이 DataSource 을 직접 사용하는 것이다. jdbc 관련된 것을 그대로 가져다 쓰는 것이니까 서비스계층에 누수되는 문제가 있다.
     * transactionManager 을 주입 받는다. 지금은 JDBC 기술을 사용하기 때문에 DataSourceTransactionManager 구현체를 주입받아야 한다.
     * transactionManager 는 dataSource 를 주입받아야 한다. 그래야 커넥션을 생성할 수 잇다.
     * 물론 JPA 를 사용하면 JpaTransactionManager 를 주입받아야 한다.
     */
//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV0 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        /**
         * 트랜잭션 시작. 파라미터로 TransactionDefinition 이란 것을 넣어줘야 한다. 속성에 대한건 나중에 알아보자. 읽기 전용... 타임아웃 등등
         * TransactionStatus 에는 현재 트랜잭션의 상태 정보가 포함되어 있다. 이후 트랜잭션을 커밋, 롤백할 때 필요하다
         * 트랜잭션을 종료하려면 동기화된 커넥션이 필요하다. 트랜잭션 동기화 매니저를 통해 동기화된 커넥션을 획득한다. 획득한 커넥션을 통해 데이터베이스에 tx에 커밋하거나 롤백한다.
         * 릴리즈는 더이상 내가할 필요가 없다. 커밋 또는 롤백시 tx가 끝나고 트랜잭션 동기화 매니저를 정리한다. 쓰레드로컬은 사용후 꼭 정리해야 한다.
         * con.setAutoCommit(true) 로 커넥션 풀을 고료해서 되돌린다. 그리고 con.close() 로 커넥션을 닫는다. 커넥션 풀을 사용하는 경우 반환
         */
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            bizLogic(fromId, toId, money);
            transactionManager.commit(status); // 성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); // 실패시 롤백
            throw new IllegalStateException(e);
        }
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}