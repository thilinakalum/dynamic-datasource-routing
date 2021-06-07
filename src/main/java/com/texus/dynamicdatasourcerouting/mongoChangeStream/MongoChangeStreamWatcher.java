package com.texus.dynamicdatasourcerouting.mongoChangeStream;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;

import lombok.extern.log4j.Log4j2;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */
@Log4j2
@Component
public class MongoChangeStreamWatcher extends Thread {

  @Value("${spring.data.mongodb.uri}")
  private String databaseConnectionString;
  @Value("${spring.data.mongodb.dbname}")
  private String databaseName;
  @Value("${spring.data.mongodb.config-collection}")
  private String collection;

  @EventListener({ApplicationReadyEvent.class})
  public void initialize() {
    start();
  }

  @Override
  public void run() {
    final ConnectionString connectionString = new ConnectionString(databaseConnectionString);
    final CodecRegistry pojoCodecRegistry =
        fromProviders(PojoCodecProvider.builder().automatic(true).build());
    final CodecRegistry codecRegistry =
        fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    final MongoClientSettings clientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .codecRegistry(codecRegistry)
            .build();

    try (final MongoClient mongoClient = MongoClients.create(clientSettings)) {
      final MongoDatabase db = mongoClient.getDatabase(databaseName);
      final MongoCollection<Document> mongoCollection = db.getCollection(collection);

      log.info("Start watching for {}", collection);
      for (final ChangeStreamDocument<Document> document : mongoCollection.watch()) {
        log.info("New document found from {}", mongoCollection);
        processDocument(document);
      }
    }
  }

  private void processDocument(final ChangeStreamDocument<Document> document) {
    try {
      final Document fullDocument = document.getFullDocument();
      if (fullDocument != null) {
        final String name = String.valueOf(fullDocument.get("name"));
        final String url = String.valueOf(fullDocument.get("url"));
        log.info("New dbconfig data found [name: {}, url:{}]", name, url);

        actuatorRefresh();
      }
    } catch (Exception ex) {
      log.info("An error occurred while sending email alert: {}", ex.getMessage());
    }
  }

  private void actuatorRefresh() {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, httpHeaders);
    restTemplate.postForEntity("http://localhost:8080/actuator/refresh", request, String.class);
  }
}
