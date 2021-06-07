package com.texus.dynamicdatasourcerouting.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */
@Data
@Entity(name = "test")
@Table(name = "test")
public class Test {

  @Id
  @GeneratedValue
  private Long id;
  private String name;
}
