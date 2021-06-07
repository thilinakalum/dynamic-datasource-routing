package com.texus.dynamicdatasourcerouting.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.texus.dynamicdatasourcerouting.entity.DBConfig;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */
@Repository
public interface DBConfigRepository extends MongoRepository<DBConfig, ObjectId> {

  DBConfig findByName(String source);
}
