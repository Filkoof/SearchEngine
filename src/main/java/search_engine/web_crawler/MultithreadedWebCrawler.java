package search_engine.web_crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import search_engine.dto.NodePage;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.repository.SiteRepository;
import search_engine.web_crawler.interfaces.PageParser;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Component
@RequiredArgsConstructor
public class MultithreadedWebCrawler extends Thread {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final PageParser pageParser;
    private final NodePage nodePage;
    private final SiteRepository siteRepository;

    @Override
    public void run() {
        forkJoinPool.execute(new RecursiveWebCrawler(pageParser, nodePage));

        while (isAlive()) {
            if (isInterrupted()) shutdownAndSetStatusFailed();
        }
    }

    private void shutdownAndSetStatusFailed() {
        forkJoinPool.shutdownNow();

        List<SiteEntity> notIndexedSites = siteRepository.findAllByStatus(StatusType.INDEXING).orElseThrow();
        notIndexedSites.forEach(site -> site.setStatus(StatusType.FAILED).setLastError("Индексация остановлена пользователем"));
        siteRepository.saveAll(notIndexedSites);
    }
}
