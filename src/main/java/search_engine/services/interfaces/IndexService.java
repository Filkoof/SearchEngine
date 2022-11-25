package search_engine.services.interfaces;

import search_engine.dto.IndexResponse;

import java.io.IOException;

public interface IndexService {

    IndexResponse startIndexing() throws IOException, InterruptedException;

    IndexResponse stopIndexing() throws InterruptedException;
}
