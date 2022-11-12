package searchengine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageDto {

    private int id;
    private String path;
    private int code;
    private String content;
}
