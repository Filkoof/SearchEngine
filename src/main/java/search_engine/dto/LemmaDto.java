package search_engine.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LemmaDto {

    private int position;
    private String incomingForm;
    private String normalForm;
}
