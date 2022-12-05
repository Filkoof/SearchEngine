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

@RequiredArgsConstructor
@Service
public class IndexServiceImpl implements IndexService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final PageParser pageParser;
    private final SitesList sites;
    private final List<Thread> threads = new ArrayList<>();

    @Override
    public IndexResponse startIndexing() {
        var sitesList = sites.getSites();
        List<NodePage> nodePages = new ArrayList<>();

        for (Site site : sitesList) {
            StatusType status = isSiteExist(site) ? siteRepository.findByUrl(site.getUrl()).getStatus() : StatusType.INDEXED;

            if (status.equals(StatusType.INDEXING)) return new IndexResponse()
                    .setResult(false)
                    .setError("Индексация уже запущена");

            deleteAllInfoFromDataBase(site);
            createAndSaveSiteEntity(site);

            NodePage nodePage = getNodePage(site.getUrl(), site.getUrl(), "");
            nodePages.add(nodePage);
        }

        nodePages.forEach(nodePage -> threads.add(new MultithreadedWebCrawler(pageParser, nodePage, siteRepository)));
        threads.forEach(Thread::start);

        return new IndexResponse().setResult(true);
    }

    private void deleteAllInfoFromDataBase(Site site) {
        if (isSiteExist(site)) {
            var siteEntity = siteRepository.findByUrl(site.getUrl());

            if (!indexRepository.findAll().isEmpty()) indexRepository.deleteAll();
            lemmaRepository.deleteAllBySiteId(siteEntity.getId());
            pageRepository.deleteAllBySiteId(siteEntity.getId());
            siteRepository.deleteById(siteEntity.getId());
        }
    }

    @Override
    public IndexResponse stopIndexing() {
        boolean isSitesNotIndexing = !siteRepository.existsByStatus(StatusType.INDEXING);
        if (isSitesNotIndexing) return new IndexResponse().setResult(false).setError("Индексация не запущена");

        threads.forEach(Thread::interrupt);

        return new IndexResponse().setResult(true);
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
        if (pageRepository.existsByPath(suffix)) deleteAllPageInfoFromDatabase(suffix);

        NodePage nodePage = getNodePage(url, prefix, suffix);
        pageParser.parseSinglePage(nodePage);

        return new IndexResponse().setResult(true);
    }

    private Site getSiteFromConfigOrNull(String url) {
        return sites.getSites().stream().filter(s -> s.getUrl().equals(url)).findFirst().orElse(null);
    }

    private void deleteAllPageInfoFromDatabase(String pagePath) throws IOException {
        Lemmatizer lemmatizer = Lemmatizer.getInstance();

        var page = pageRepository.findByPath(pagePath);
        var lemmas = lemmatizer.getLemmaSet(page.getContent());

        lemmas.forEach(lemma -> lemmaRepository.decrementAllFrequencyBySiteIdAndLemma(page.getSite().getId(), lemma));
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
                .setTimeBetweenRequest(50)
                .setSiteId(siteEntity.getId());
    }
}