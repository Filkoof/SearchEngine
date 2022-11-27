package search_engine.web_crawler;

import lombok.RequiredArgsConstructor;
import search_engine.dto.NodePage;
import search_engine.entity.enumerated.StatusType;
import search_engine.repository.SiteRepository;
import search_engine.web_crawler.interfaces.PageParser;

import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
public class MultithreadedWebCrawler extends Thread {

    private final ForkJoinPool forkJoinPool;
    private final PageParser pageParser;
    private final NodePage nodePage;
    private final SiteRepository siteRepository;

    @Override
    public void run() {
        forkJoinPool.invoke(new RecursiveWebCrawler(pageParser, nodePage));
        setStatusIndexed();
    }

    private void setStatusIndexed() {
        var indexingSites = siteRepository.findAllByStatus(StatusType.INDEXING).orElseThrow();
        indexingSites.forEach(site -> site.setStatus(StatusType.INDEXED));
        siteRepository.saveAll(indexingSites);
    }
}
