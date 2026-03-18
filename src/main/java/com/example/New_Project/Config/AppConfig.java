package com.example.New_Project.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /**
     * Provides a {@link Clock} bean so that {@code LocalDateTime.now(clock)}
     * can be used in services instead of the static {@code LocalDateTime.now()}.
     * This makes time mockable in unit tests:
     * {@code when(clock.instant()).thenReturn(...)}
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
