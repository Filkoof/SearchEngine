package searchengine.services.interfaces;

import searchengine.dto.ResponseDto;

import java.io.IOException;
import java.util.Map;

public interface IndexService {

    ResponseDto<Map<String, Boolean>> indexing() throws IOException, InterruptedException;
}
