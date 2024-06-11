package org.foch.application.utils;

import com.vaadin.flow.spring.annotation.SpringComponent;
import org.foch.application.data.InMemoryData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
public class InitData {

    @Bean
    public CommandLineRunner loadData(
            @Value("${server.url}") String serverUrl
    ) {
        return args -> InMemoryData.serverUrl = serverUrl;
    }

}