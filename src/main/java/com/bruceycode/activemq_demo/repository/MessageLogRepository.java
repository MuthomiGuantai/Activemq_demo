package com.bruceycode.activemq_demo.repository;

import com.bruceycode.activemq_demo.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

}
