package com.uphill.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for query performance monitoring.
 * This configuration helps identify and resolve N+1 query problems.
 */
@Configuration
public class QueryPerformanceConfig {

    /**
     * Enable Hibernate statistics for performance monitoring.
     * This helps identify N+1 queries and other performance issues.
     */
    @Bean
    @Profile("!prod")
    public String enableHibernateStats() {
        System.setProperty("hibernate.generate_statistics", "true");
        System.setProperty("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS", "100");
        return "Hibernate statistics enabled for performance monitoring";
    }
}
