package searchengine.webCrawler;

import lombok.RequiredArgsConstructor;
import searchengine.dto.NodePage;
import searchengine.webCrawler.interfaces.PageParser;

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
