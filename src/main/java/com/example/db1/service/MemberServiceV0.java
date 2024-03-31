package com.example.db1.service;

import com.example.db1.domain.Member;
import com.example.db1.repository.MemberRepositoryV0;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV0 {

    private final MemberRepositoryV0 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //시작
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        /**
         * 오류케이스를 만들기 위함.. 검증에 실패하면 다음으로 못 넘어감
         */
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
        //커밋, 롤백
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}