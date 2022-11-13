package searchengine.services.interfaces;

import java.io.IOException;

public interface WebCrawlerService {

    void crawl(String rootURL) throws InterruptedException, IOException;
}
