package search_engine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import search_engine.config.Site;
import search_engine.config.SitesList;
import search_engine.dto.IndexResponse;
import search_engine.dto.NodePage;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.lemmatizer.Lemmatizer;
import search_engine.repository.IndexRepository;
import search_engine.repository.LemmaRepository;
import search_engine.repository.PageRepository;
import search_engine.repository.SiteRepository;
import search_engine.services.interfaces.IndexService;
import search_engine.web_crawler.MultithreadedWebCrawler;
import search_engine.web_crawler.interfaces.PageParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Service
public class IndexServiceImpl implements IndexService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final PageParser pageParser;
    private final SitesList sites;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    @Override
    public IndexResponse startIndexing() {
        var sitesList = sites.getSites();
        List<NodePage> nodePages = new ArrayList<>();

        for (Site site : sitesList) {
            StatusType status = isSiteExist(site) ? siteRepository.findByUrl(site.getUrl()).getStatus() : StatusType.INDEXED;

            if (status.equals(StatusType.INDEXING)) return new IndexResponse()
                    .setResult(false)
                    .setError("Индексация уже запущена");

            if (isSiteExist(site)) deleteAllSiteInfoFromDataBase(site);
            createAndSaveSiteEntity(site);

            NodePage nodePage = getNodePage(site.getUrl(), site.getUrl(), "");
            nodePages.add(nodePage);
        }

        nodePages.forEach(nodePage -> new MultithreadedWebCrawler(forkJoinPool, pageParser, nodePage, siteRepository).start());

        return new IndexResponse().setResult(true);
    }

    private void deleteAllSiteInfoFromDataBase(Site site) {
        var siteEntity = siteRepository.findByUrl(site.getUrl());
        var pages = pageRepository.findAllBySiteId(siteEntity.getId());

        pages.forEach(page -> indexRepository.deleteAllByPageId(page.getId()));
        lemmaRepository.deleteAllBySiteId(siteEntity.getId());
        pageRepository.deleteAllBySiteId(siteEntity.getId());
        siteRepository.deleteById(siteEntity.getId());
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
        List<SiteEntity> notIndexedSites = siteRepository.findAllByStatus(StatusType.INDEXING).orElseThrow();
        notIndexedSites.forEach(site -> site.setStatus(StatusType.FAILED).setLastError("Индексация остановлена пользователем"));
        siteRepository.saveAll(notIndexedSites);
    }

    @Override
    public IndexResponse indexPage(String url) throws IOException {
        String[] pathElements = url.split("/");
        String prefix = pathElements[0] + "//" + pathElements[1] + pathElements[2];
        String suffix = url.replaceAll(prefix, "");

        Site site = getSiteFromConfigOrNull(prefix);
        if (site == null) return new IndexResponse().setResult(false)
                .setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");

        if (!isSiteExist(site)) createAndSaveSiteEntity(site);
        if (pageRepository.existsByPath(url)) deleteAllPageInfoFromDatabase(url);

        NodePage nodePage = getNodePage(url, prefix, suffix);
        pageParser.parseSinglePage(nodePage);

        return new IndexResponse().setResult(true);
    }

    private Site getSiteFromConfigOrNull(String url) {
        return sites.getSites().stream().filter(s -> s.getUrl().equals(url)).findFirst().orElse(null);
    }

    private void deleteAllPageInfoFromDatabase(String pageUrl) throws IOException {
        Lemmatizer lemmatizer = Lemmatizer.getInstance();

        var page = pageRepository.findByPath(pageUrl);
        var lemmas = lemmatizer.getLemmaSet(page.getContent());

        lemmas.forEach(lemmaRepository::decrementAllFrequencyByLemma);
        indexRepository.deleteAllByPageId(page.getId());
        pageRepository.delete(page);
    }

    private boolean isSiteExist(Site site) {
        return siteRepository.existsByUrl(site.getUrl());
    }

    private void createAndSaveSiteEntity(Site site) {
        siteRepository.save(new SiteEntity().setStatus(StatusType.INDEXING)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl(site.getUrl())
                .setName(site.getName()));
    }

    private NodePage getNodePage(String path, String suffix, String prefix) {
        var siteEntity = siteRepository.findByUrl(suffix);
        return new NodePage().setPath(path)
                .setSuffix(suffix)
                .setPrefix(prefix)
                .setTimeBetweenRequest(150)
                .setSiteId(siteEntity.getId());
    }
}