package com.mailProject.email.repository;

import com.mailProject.email.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepo extends JpaRepository <Email, Long> {

}
