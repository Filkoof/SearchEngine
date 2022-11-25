package search_engine.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import search_engine.ContextLoad;
import search_engine.dto.statistics.StatisticsResponse;
import search_engine.services.interfaces.StatisticsService;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsServiceTests extends ContextLoad {

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