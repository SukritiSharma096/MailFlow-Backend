package com.mailProject.email.repository;

import com.mailProject.email.entity.ReceivedEmails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiveEmailRepository extends JpaRepository<ReceivedEmails, Long> {

    Optional<ReceivedEmails> findByIdAndAccountId(Long id, Long accountId);

    Optional<ReceivedEmails> findById(Long id);

    Optional<ReceivedEmails> findByMessageId(String messageId);

    List<ReceivedEmails> findTop20ByAccountIdAndFolderOrderBySentAtDesc(Long accountId, String folder);


}
