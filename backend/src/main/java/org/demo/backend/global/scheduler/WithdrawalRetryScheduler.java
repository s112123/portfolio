package org.demo.backend.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.demo.backend.member.domain.WithdrawalTaskFailLog;
import org.demo.backend.member.persistence.WithdrawalTaskFailLogRepository;
import org.demo.backend.member.event.worker.WithdrawalRetryWorker;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WithdrawalRetryScheduler {

    private final WithdrawalTaskFailLogRepository failLogRepository;
    private final WithdrawalRetryWorker withdrawalRetryWorker;

    // 10분마다 실패한 회원 탈퇴 처리 재시도
    @Scheduled(fixedDelay = 600_000)
    public void retryWithdrawal() {
        int chunkSize = 100;
        Pageable pageable = PageRequest.of(0, chunkSize);

        while (true) {
            // 1. 탈퇴 시, 실패한 로그 조회
            List<WithdrawalTaskFailLog> failLogs = failLogRepository.findByRetryLessThan(
                    WithdrawalRetryWorker.MAX_RETRY_COUNT,
                    pageable
            ).getContent();
            if (failLogs.isEmpty()) break;

            // 2. 삭제 재시도
            for (WithdrawalTaskFailLog failLog : failLogs) {
                withdrawalRetryWorker.process(failLog);
            }

            // 3. 조회된 데이터가 없으면 반복문 탈출
            if (failLogs.size() < chunkSize) break;
        }
    }
}
