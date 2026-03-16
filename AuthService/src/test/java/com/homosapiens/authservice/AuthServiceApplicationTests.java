package com.homosapiens.authservice;

import com.homosapiens.authservice.core.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AuthServiceApplicationTests {

    @MockBean
    private KafkaProducer kafkaProducer;

    @Test
    void contextLoads() {
    }

}
