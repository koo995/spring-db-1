package com.example.db1.service;

import com.example.db1.domain.Member;
import com.example.db1.repository.MemberRepositoryV0;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜잭션을 넣을 것이다. - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV0 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV0 memberRepository;

    /**
     * 이번에는 @RequiredArgsConstructor 를 사용하지 않았다.
     * TransactionTemplate 을 쓸려면 transactionManager 가 필요하다.
     * 변하는 부분과 변하지 않는 부분을 분리하기 위함이며, transactionManager 의 기능은 변하지 않는 부분에 해당된다.
     */
    public MemberServiceV0(PlatformTransactionManager transactionManager, MemberRepositoryV0 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        txTemplate.executeWithoutResult(status -> {
            /**
             * 비즈니스 로직.
             * txTemplate 덕분에 커밋하거나 롤백하는 코드가 모두 제거되었다.
             * "언체크 예외" 가 발생하면 롤백한다. "체크 예외" 가 발생하면 커밋한다. 뒤에서 설명.
             * SQLException 은 "체크 예외" 인데, 해당 람다에서 체크 예외를 밖으로 던질 수 없기 때문에
             * "언체크" 예외로 바꾸어 던지도록 예외를 전환했다.
             */
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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