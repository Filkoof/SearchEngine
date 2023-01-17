package search_engine.dto.statistics;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TotalStatistics {
    private long sites;
    private long pages;
    private long lemmas;
    private boolean indexing;
}
