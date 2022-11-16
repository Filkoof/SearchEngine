package searchengine.services.interfaces;

import searchengine.dto.ResponseDto;

import java.io.IOException;
import java.util.Map;

public interface IndexService {

    ResponseDto<Map<String, Boolean>> startIndexing() throws IOException, InterruptedException;

    ResponseDto<Map<String, Boolean>> stopIndexing() throws InterruptedException;
}
