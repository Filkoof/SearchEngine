package search_engine.web_crawler;

import lombok.RequiredArgsConstructor;
import search_engine.dto.NodePage;
import search_engine.web_crawler.interfaces.PageParser;

import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
public class MultithreadedWebCrawler extends Thread {

    private final ForkJoinPool forkJoinPool;
    private final PageParser pageParser;
    private final NodePage nodePage;

    @Override
    public void run() {
        forkJoinPool.invoke(new RecursiveWebCrawler(pageParser, nodePage));
    }
}
