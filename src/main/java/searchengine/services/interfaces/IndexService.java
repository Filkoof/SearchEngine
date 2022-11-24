package searchengine.services.interfaces;

import searchengine.dto.IndexResponse;

import java.io.IOException;

public interface IndexService {

    IndexResponse startIndexing() throws IOException, InterruptedException;

    IndexResponse stopIndexing() throws InterruptedException;
}
