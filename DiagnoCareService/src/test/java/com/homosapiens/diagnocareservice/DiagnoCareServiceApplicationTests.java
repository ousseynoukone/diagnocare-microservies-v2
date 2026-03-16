package com.homosapiens.diagnocareservice;

import com.homosapiens.diagnocareservice.core.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class DiagnoCareServiceApplicationTests {

	@MockBean
	private KafkaProducer kafkaProducer;

	@Test
	void contextLoads() {
	}

}
