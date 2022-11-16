package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.NodePage;
import searchengine.dto.ResponseDto;
import searchengine.entity.SiteEntity;
import searchengine.entity.enumerated.StatusType;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.interfaces.IndexService;
import searchengine.webCrawler.RecursiveWebCrawler;
import searchengine.webCrawler.interfaces.PageParser;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Service
public class IndexServiceImpl implements IndexService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final PageParser pageParser;
    private final SitesList sites;
    private final ForkJoinPool forkJoinPool;


    /*
     * TODO:
     *  1. Придумать как в startIndexing реализовать верно обработку листа сайтов
     *  2. Протестировать!
     * */

    @Override
    public ResponseDto<Map<String, Boolean>> startIndexing() {
        var site = sites.getSites().get(0);

        deleteAllInfoFromDataBase(site);
        var siteId = saveSiteInDataBaseAndReturnId(site);

        NodePage nodePage = new NodePage();
        nodePage.setPath(site.getUrl())
                .setSuffix(site.getUrl())
                .setPrefix("")
                .setTimeBetweenRequest(150)
                .setSiteId(siteId);

        forkJoinPool.invoke(new RecursiveWebCrawler(pageParser, nodePage));

        var status = siteRepository.findByUrl(site.getUrl()).getStatus();
        return ResponseDto.<Map<String, Boolean>>builder()
                .data(Map.of("result", !status.equals(StatusType.INDEXING)))
                .error(status.equals(StatusType.INDEXING) ? "Индексация  уже запущена" : "")
                .build();

    }

    private void deleteAllInfoFromDataBase(Site site) {
        var siteEntity = siteRepository.findByUrl(site.getUrl());
        if (siteEntity != null) {
            if (pageRepository.existsBySiteId(siteEntity.getId())) pageRepository.deleteAllBySiteId(siteEntity.getId());
            siteRepository.deleteById(siteEntity.getId());
        }
    }

    private int saveSiteInDataBaseAndReturnId(Site site) {
        return siteRepository.save(new SiteEntity().setStatus(StatusType.INDEXING)
                        .setStatusTime(LocalDateTime.now())
                        .setLastError(null)
                        .setUrl(site.getUrl())
                        .setName(site.getName()))
                .getId();
    }

    @Override
    public ResponseDto<Map<String, Boolean>> stopIndexing() {
        boolean isIndexing;

        if (forkJoinPool.isShutdown()) {
            isIndexing = false;
        } else {
            isIndexing = true;
            forkJoinPool.shutdown();
        }

        return ResponseDto.<Map<String, Boolean>>builder()
                .data(Map.of("result", isIndexing))
                .error(isIndexing ? "" : "Индексация не запущена")
                .build();
    }
}