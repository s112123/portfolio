package org.demo.backend.member.persistence;

import org.demo.backend.member.domain.WithdrawalTaskFailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalTaskFailLogRepository extends JpaRepository<WithdrawalTaskFailLog, Long> {

    // retry 가 특정 횟수 미만인 것만 페이징 조회
    Page<WithdrawalTaskFailLog> findByRetryLessThan(int retry, Pageable pageable);
}
