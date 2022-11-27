package search_engine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import search_engine.config.Site;
import search_engine.config.SitesList;
import search_engine.dto.statistics.DetailedStatisticsItem;
import search_engine.dto.statistics.StatisticsData;
import search_engine.dto.statistics.StatisticsResponse;
import search_engine.dto.statistics.TotalStatistics;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.repository.LemmaRepository;
import search_engine.repository.PageRepository;
import search_engine.repository.SiteRepository;
import search_engine.services.interfaces.StatisticsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SitesList sites;

    @Override
    public StatisticsResponse getStatistics() {
        List<Site> siteList = sites.getSites();

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        siteList.forEach(site -> detailed.add(getDetailedStatistic(site)));

        TotalStatistics total = getTotalStatistic();

        return new StatisticsResponse()
                .setResult(true)
                .setStatistics(getStatistic(total, detailed));
    }

    private DetailedStatisticsItem getDetailedStatistic(Site site) {
        boolean isSiteExist = siteRepository.existsByUrl(site.getUrl());
        SiteEntity siteEntity = null;
        int pagesCount = 0;
        int lemmasCount = 0;

        if (isSiteExist) {
            siteEntity = siteRepository.findByUrl(site.getUrl());
            pagesCount = pageRepository.countAllBySiteId(siteEntity.getId());
            lemmasCount = lemmaRepository.countAllBySiteId(siteEntity.getId());
        }

        return new DetailedStatisticsItem()
                .setUrl(site.getUrl())
                .setName(site.getName())
                .setStatus(isSiteExist ? siteEntity.getStatus() : null)
                .setStatusTime(isSiteExist ? siteEntity.getStatusTime() : LocalDateTime.now())
                .setError(isSiteExist ? siteEntity.getLastError() : null)
                .setPages(pagesCount)
                .setLemmas(lemmasCount);
    }

    private TotalStatistics getTotalStatistic() {
        int sitesCount = siteRepository.findAll().size();
        int pagesCount = pageRepository.findAll().size();
        int lemmaCount = lemmaRepository.findAll().size();
        var indexingSites = siteRepository.findAllByStatus(StatusType.INDEXING);
        return new TotalStatistics()
                .setSites(sitesCount)
                .setPages(pagesCount)
                .setLemmas(lemmaCount)
                .setIndexing(indexingSites.isPresent());
    }

    private StatisticsData getStatistic(TotalStatistics  total, List<DetailedStatisticsItem> detailed) {
        return new StatisticsData()
                .setTotal(total)
                .setDetailed(detailed);
    }
}
