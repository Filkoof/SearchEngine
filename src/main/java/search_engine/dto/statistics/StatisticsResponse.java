package search_engine.dto.statistics;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
