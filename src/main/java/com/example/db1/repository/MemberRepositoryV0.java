package com.example.db1.repository;

import com.example.db1.connection.DBConnectionUtil;
import com.example.db1.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * jdbc - DriverManager 를 이용한 회원 저장소
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            /**
             * executeQuery 는 select 할 때 사용한다.
             * rs 는 결과를 담는 객체이다.
             */
            rs = pstmt.executeQuery();
            /**
             * rs.next() 는 그 안에 커서가 존재하기 때문에 처음에는 아무것도 안가르킨다.
             * 데이터가 여러개인 경우 while 문을 사용한다.
             */
            if (rs.next()) {
                return new Member(rs.getString("member_id"), rs.getInt("money"));
            }
            return null;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member (member_id, money) values (?, ?)";

        Connection con = null;
        /**
         * 이것을 이용해서 쿼리를 날린다.
         * Statement 을 상속받은.. 물음표를 통한 파라미터 바인딩이 가능하다.
         * 이런걸 쓰는 이유는 sql injection 을 방지하기 위해서이다.
         */
        PreparedStatement pstmt = null;

        /**
         * checked exception 이 올라와서 잡던가 던져야 한다.
         */
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            /**
             * 물음표에 파라미터 바인딩을 해준다.
             * 타입 정보까지 줄 수 있다.
             */
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            /**
             * 'Statement' 를 통해 준비된 sql 을 커넥션을 통해 실제 데이터베이스에 전달한다.
             */
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }
        finally {
            /**
             * 이것을 꼭 닫아줘야 한다. 안그러면 db connection 이 계속 쌓이게 된다. 순서는 역순으로
             * 이 부분을 놓치게 되면 커넥션이 끊어지지 않고 계속 유지되는 문제가 발생할 수 있다. 이런 것을 리소스 누수라고 하는데, 결과적으로 커넥션 부족으로 장애가 발생할 수 있다.
             */
            close(con, pstmt, null);
        }
    }

    private static Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("rs close error", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("stmt close error", e);
            }}

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("connection close error", e);
            }
        }
    }
}
