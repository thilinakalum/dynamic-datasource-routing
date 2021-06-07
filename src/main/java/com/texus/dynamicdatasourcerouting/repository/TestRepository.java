package com.texus.dynamicdatasourcerouting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.texus.dynamicdatasourcerouting.entity.Test;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */
@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

}
