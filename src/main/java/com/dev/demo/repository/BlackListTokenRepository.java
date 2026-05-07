package com.dev.demo.repository;

import com.dev.demo.entity.BlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListTokenRepository extends JpaRepository<BlackListToken, String> {

}
