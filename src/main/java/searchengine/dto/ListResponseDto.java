package searchengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListResponseDto<T> {

    private List<T> data;
    private String error;
    private LocalDateTime timestamp;
}
