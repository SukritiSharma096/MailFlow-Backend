package com.mailProject.email.repository;

import com.mailProject.email.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface AdminRepository  extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);

    List<Admin> findAllByActiveTrue();
}
