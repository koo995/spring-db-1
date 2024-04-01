package com.example.db1.service;

import com.example.db1.domain.Member;
import com.example.db1.repository.MemberRepositoryV0;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - @Transactional AOP
 * 스프링이 제공하는 AOP을 쓰고 싶은데, 스프링 컨테이너를 안쓰면 @Transactional 이 동작하지 않는다.
 * @SpringBootTest 이 있으면 테스트시 스프링 부트를 통해 스프링 컨테이너를 생성한다. 그리고 @Autowired 를 통해 의존성을 주입받을 수 있다.
 */
@Slf4j
@SpringBootTest
class MemberServiceTest {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV0 memberRepository;
    @Autowired
    private MemberServiceV0 memberService;

    /**
     * 스프링 컨테이너에 빈을 넣어줘야하고 조립을 해줘야한다.
     * @TestConfiguration 은 테스트 안에서 내부 설정 클래스를 만들어서 사용하면서 이 에노테이션을 붙이면,
     * 스프링 부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있다.
     */
    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepositoryV0 memberRepository() {
            return new MemberRepositoryV0(dataSource);
        }
        @Bean
        MemberServiceV0 memberService(MemberRepositoryV0 memberRepository) {
            return new MemberServiceV0(memberRepository);
        }
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void AopCheck() {
        // memberRepository class=class com.example.db1.repository.MemberRepositoryV0
        log.info("memberRepository class={}", memberRepository.getClass());
        // memberService class=class com.example.db1.service.MemberServiceV0$$SpringCGLIB$$0
        log.info("memberService class={}", memberService.getClass());
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        /**
         * given 과 then 에 있는 커넥션과 다른 커넥션을 쓸것이다.
         * 그리고 로그가 안남는 이유는... DriverManagerDataSource 이 녀석을 통해 바로 커넥션을 가져왔기 때문
         */
        log.info("START TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        //when
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());
        /**
         * 롤백을 해버려서 값이 그대로이다.
         */
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }

}
