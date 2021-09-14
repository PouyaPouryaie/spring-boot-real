package ir.bigz.springbootreal.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DataSourceConfiguration {

    @Autowired
    private Environment env;


    @Bean
    @ConfigurationProperties(prefix = "demo.datasource")
    public DataSourceProperties dataSourceProperties(){
        return new DataSourceProperties();
    }


    @Bean
    public HikariDataSource dataSource(){
        DataSourceProperties dataSourceProperties = dataSourceProperties();
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setConnectionTimeout(Integer.parseInt(env.getProperty("spring.datasource.hikari.connectionTimeout")));
        dataSource.setMaximumPoolSize(Integer.parseInt(env.getProperty("spring.datasource.hikari.maximumPoolSize")));
        return dataSource;
//        return DataSourceBuilder.create()
//                .driverClassName(dataSourceProperties.getDriverClassName())
//                .url(dataSourceProperties.getUrl())
//                .username(dataSourceProperties.getUsername())
//                .password(dataSourceProperties.getPassword())
//                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource());
        factory.setPackagesToScan(new String[]{"ir.bigz.springbootreal.dao"});
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("demo.jpa.hibernate.ddl-auto"));
        jpaProperties.put("hibernate.show-sql", env.getProperty("demo.jpa.show-sql"));
        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(){
        EntityManagerFactory factory = entityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }


}
