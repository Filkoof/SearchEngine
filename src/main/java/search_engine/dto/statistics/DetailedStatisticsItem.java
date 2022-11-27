package search_engine.dto.statistics;

import lombok.Data;
import lombok.experimental.Accessors;
import search_engine.entity.enumerated.StatusType;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private StatusType status;
    private LocalDateTime statusTime;
    private String error;
    private int pages;
    private int lemmas;
}
