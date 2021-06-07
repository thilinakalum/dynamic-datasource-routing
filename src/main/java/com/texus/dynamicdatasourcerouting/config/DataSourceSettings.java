package com.texus.dynamicdatasourcerouting.config;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */

@RefreshScope
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.texus.dynamicdatasourcerouting", entityManagerFactoryRef = "entityManager", transactionManagerRef = "multiTransactionManager")
public class DataSourceSettings implements BeanFactoryAware {

  @Value("${spring.data.mongodb.uri}")
  private String databaseConnectionString;
  @Value("${spring.data.mongodb.dbname}")
  private String databaseName;
  @Value("${spring.data.mongodb.config-collection}")
  private String collection;

  private BeanFactory beanFactory;
  private Map<Object, Object> clientDataSourcesMap = new HashMap<>();

  @RefreshScope
  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Bean(name = "testDatasource")
  @ConfigurationProperties(value = "app.datasource.test")
  public DataSource testDatasource() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }

  @RefreshScope
  @PostConstruct
  public void configure() {
    final Map<Object, Object> clientDataSources = new HashMap<>();

    final ConnectionString connectionString = new ConnectionString(databaseConnectionString);
    final CodecRegistry pojoCodecRegistry = fromProviders(
        PojoCodecProvider.builder().automatic(true).build());
    final CodecRegistry codecRegistry = fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    final MongoClientSettings clientSettings = MongoClientSettings.builder()
        .applyConnectionString(connectionString).codecRegistry(codecRegistry).build();

    try (final MongoClient mongoClient = MongoClients.create(clientSettings)) {
      final MongoDatabase database = mongoClient.getDatabase(databaseName);
      MongoCollection<Document> collection1 = database.getCollection(collection);
      for (Document document : collection1.find()) {
        clientDataSources.put(document.get("name"),
            createDataSource(String.valueOf(document.get("url")),
                String.valueOf(document.get("username")),
                String.valueOf(document.get("password"))));
      }
    }

    Assert.state(beanFactory instanceof ConfigurableBeanFactory, "wrong bean factory type");
    ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
    for (Map.Entry<Object, Object> entry : clientDataSources.entrySet()) {
      DataSource dataSource = (DataSource) entry.getValue();
      configurableBeanFactory
          .registerSingleton(entry.getKey().toString() + Math.random(), dataSource);
    }
    clientDataSourcesMap = clientDataSources;
  }

  @RefreshScope
  private DataSource createDataSource(String url, String username, String password) {
    return DataSourceBuilder.create().type(HikariDataSource.class).url(url).username(username)
        .password(password).build();
  }

  @RefreshScope
  @Bean(name = "dataSource")
  public DataSource dataSource() {
    com.texus.dynamicdatasourcerouting.config.MultiRoutingDataSource routingDataSource = new com.texus.dynamicdatasourcerouting.config.MultiRoutingDataSource();
    routingDataSource.setDefaultTargetDataSource(testDatasource());
    routingDataSource.setTargetDataSources(clientDataSourcesMap);
    return routingDataSource;
  }

  @Bean(name = "entityManager")
  public LocalContainerEntityManagerFactoryBean entityManager() throws PropertyVetoException {

    LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
    entityManagerFactory.setDataSource(dataSource());
    entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    entityManagerFactory.setPackagesToScan("com.texus.dynamicdatasourcerouting.entity");
    return entityManagerFactory;
  }

  @Bean(name = "multiTransactionManager")
  public PlatformTransactionManager multiTransactionManager() throws PropertyVetoException {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManager().getObject());
    return transactionManager;
  }
}
