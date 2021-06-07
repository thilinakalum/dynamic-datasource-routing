package com.texus.dynamicdatasourcerouting.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.texus.dynamicdatasourcerouting.entity.Test;
import com.texus.dynamicdatasourcerouting.repository.TestRepository;

import lombok.extern.log4j.Log4j2;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */
@Log4j2
@RestController
@RequestMapping("test")
public class TestController {

  @Autowired
  private TestRepository testRepository;

  @GetMapping()
  public ResponseEntity<List<Test>> getAll(
      @RequestParam(name = "source") String source) {

    return ResponseEntity.ok().body(testRepository.findAll());
  }
}
