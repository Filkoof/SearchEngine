package searchengine.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.interfaces.StatisticsService;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsServiceTests {

    @Autowired
    StatisticsService statisticsService;

    @Test
    @DisplayName("Statistics service test")
    void getStatistics() {
        var statisticsResponse = new StatisticsResponse();
        statisticsResponse.setResult(true);

        assertEquals(statisticsResponse.isResult(), statisticsService.getStatistics().isResult());
    }
}