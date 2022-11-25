package search_engine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import search_engine.config.Site;
import search_engine.config.SitesList;
import search_engine.dto.IndexResponse;
import search_engine.dto.NodePage;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.repository.PageRepository;
import search_engine.repository.SiteRepository;
import search_engine.services.interfaces.IndexService;
import search_engine.web_crawler.MultithreadedWebCrawler;
import search_engine.web_crawler.interfaces.PageParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Service
public class IndexServiceImpl implements IndexService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final PageParser pageParser;
    private final SitesList sites;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    @Override
    public IndexResponse startIndexing() {
        var sitesList = sites.getSites();
        List<NodePage> nodePages = new ArrayList<>();

        for (Site site : sitesList) {
            var isSiteExist = siteRepository.existsByUrl(site.getUrl());
            var status = isSiteExist ? siteRepository.findByUrl(site.getUrl()).getStatus() : StatusType.INDEXED;

            if (status.equals(StatusType.INDEXING)) return new IndexResponse()
                    .setResult(false)
                    .setError("Индексация уже запущена");

            var siteEntity = getSiteEntity(site);

            if (isSiteExist) deleteAllInfoFromDataBase(site);
            saveSiteInDataBase(siteEntity);

            var nodePage = getNodePage(site);
            nodePages.add(nodePage);
        }

        nodePages.forEach(nodePage -> new MultithreadedWebCrawler(forkJoinPool, pageParser, nodePage).start());
        setStatusIndexed();

        return new IndexResponse().setResult(true);
    }

    private SiteEntity getSiteEntity(Site site) {
        return new SiteEntity().setStatus(StatusType.INDEXING)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl(site.getUrl())
                .setName(site.getName());
    }

    private void deleteAllInfoFromDataBase(Site site) {
        var siteEntity = siteRepository.findByUrl(site.getUrl());
        if (siteEntity != null) {
            if (pageRepository.existsBySiteId(siteEntity.getId())) pageRepository.deleteAllBySiteId(siteEntity.getId());
            siteRepository.deleteById(siteEntity.getId());
        }
    }

    private void saveSiteInDataBase(SiteEntity siteEntity) {
        if (siteEntity != null) siteRepository.save(siteEntity);
    }

    private NodePage getNodePage(Site site) {
        var siteEntity = siteRepository.findByUrl(site.getUrl());
        return new NodePage().setPath(site.getUrl())
                .setSuffix(site.getUrl())
                .setPrefix("")
                .setTimeBetweenRequest(150)
                .setSiteId(siteEntity.getId());
    }

    private void setStatusIndexed() {
        var indexingSites = siteRepository.findAllByStatus(StatusType.INDEXING);
        indexingSites.forEach(site -> site.setStatus(StatusType.INDEXED));
        siteRepository.saveAll(indexingSites);
    }

    @Override
    public IndexResponse stopIndexing() {
        if (forkJoinPool.isShutdown()) {
            return new IndexResponse().setResult(false).setError("Индексация не запущена");
        } else {
            forkJoinPool.shutdown();
            setStatusFailedForNotIndexedSites();
        }

        return new IndexResponse().setResult(true);
    }

    private void setStatusFailedForNotIndexedSites() {
        var notIndexedSites = siteRepository.findAllByStatus(StatusType.INDEXING);
        notIndexedSites.forEach(site -> site.setStatus(StatusType.FAILED).setLastError("Индексация остановлена пользователем"));
        siteRepository.saveAll(notIndexedSites);
    }
}