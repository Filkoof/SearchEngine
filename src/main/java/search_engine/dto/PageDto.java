package search_engine.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageDto {

    private int id;
    private int siteId;
    private String path;
    private int code;
    private String content;
}


