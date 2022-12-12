package search_engine.web_crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import search_engine.dto.NodePage;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.repository.SiteRepository;
import search_engine.web_crawler.interfaces.PageParser;

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
        var site = siteRepository.findById(nodePage.getSiteId()).orElseThrow();

        while (isAlive()) {
            if (isInterrupted()) shutdownAndSetStatusFailed(site);
        }

        site.setStatus(StatusType.INDEXED);
        siteRepository.save(site);
        interrupt();
    }

    private void shutdownAndSetStatusFailed(SiteEntity site) {
        forkJoinPool.shutdownNow();

        site.setStatus(StatusType.FAILED).setLastError("Индексация остановлена пользователем");
        siteRepository.save(site);
    }
}
