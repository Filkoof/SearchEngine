package search_engine.web_crawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import search_engine.dto.NodePage;
import search_engine.entity.LemmaEntity;
import search_engine.entity.PageEntity;
import search_engine.entity.SearchIndexEntity;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.lemmatizer.Lemmatizer;
import search_engine.repository.IndexRepository;
import search_engine.repository.LemmaRepository;
import search_engine.repository.PageRepository;
import search_engine.repository.SiteRepository;
import search_engine.web_crawler.interfaces.PageParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class PageParserImpl implements PageParser {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Override
    public synchronized void startPageParser(NodePage nodePage) {
        var siteEntity = siteRepository.findById(nodePage.getSiteId()).orElseThrow();
        updateStatusTime(siteEntity);

        try {
            Document document = jsoupConnect(nodePage.getPath());
            var elements = document.select("a[href]");

            List<PageEntity> pages = new ArrayList<>();
            for (Element element : elements) {
                var subPath = element.attr("abs:href");
                var page = getPageEntity(subPath, siteEntity, document);
                var referenceOnChildSet = nodePage.getReferenceOnChildSet();

                if (isNeedSave(page.getPath())) {
                    pages.add(page);
                    referenceOnChildSet.add(subPath);
                }
            }
            pageRepository.saveAll(pages);
            saveLemmaAndIndex(pages);
        } catch (IOException e) {
            setStatusFailedAndErrorMessage(siteEntity, e.toString());
            throw new RuntimeException(e.getMessage());
        }
    }

    private boolean isNeedSave(String path) {
        return !pageRepository.existsByPath(path)
                && path.startsWith("/")
                && !path.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf|xml))$)")
                && !path.contains("#");
    }

    @Override
    public void parseSinglePage(NodePage nodePage) {
        var siteEntity = siteRepository.findById(nodePage.getSiteId()).orElseThrow();
        siteRepository.save(siteEntity.setStatus(StatusType.INDEXING));
        updateStatusTime(siteEntity);

        try {
            Document document = jsoupConnect(nodePage.getPath());

            var page = getPageEntity(nodePage.getPath(), siteEntity, document);
            pageRepository.save(page);
            saveLemmaAndIndex(List.of(page));
        } catch (IOException e) {
            setStatusFailedAndErrorMessage(siteEntity, e.toString());
            throw new RuntimeException(e.getMessage());
        }

        siteRepository.save(siteEntity.setStatus(StatusType.INDEXED));
    }

    private synchronized void saveLemmaAndIndex(List<PageEntity> pageEntities) throws IOException {
        Lemmatizer lemmatizer = Lemmatizer.getInstance();

        List<LemmaEntity> lemmaEntities = new ArrayList<>();
        List<SearchIndexEntity> searchIndexEntities = new ArrayList<>();

        for (PageEntity pageEntity : pageEntities) {
            int siteId = pageEntity.getSite().getId();

            Map<String, Integer> lemmas = lemmatizer.collectLemmas(pageEntity.getContent());
            for (Map.Entry<String, Integer> word : lemmas.entrySet()) {
                var lemma = word.getKey();

                LemmaEntity lemmaEntity;
                if (lemmaRepository.existsBySiteIdAndLemma(siteId, lemma)) {
                    lemmaEntity = lemmaRepository.findBySiteIdAndLemma(siteId, lemma).orElseThrow();
                    lemmaEntity.setFrequency(Math.incrementExact(lemmaEntity.getFrequency()));
                    lemmaEntities.add(lemmaEntity);
                } else {
                    lemmaEntity = new LemmaEntity()
                            .setSite(pageEntity.getSite())
                            .setLemma(lemma)
                            .setFrequency(1);
                    lemmaEntities.add(lemmaEntity);
                }

                if (lemmaEntities.size() >= 100) {
                    lemmaRepository.saveAll(lemmaEntities);
                    lemmaEntities.clear();
                }

                searchIndexEntities.add(new SearchIndexEntity()
                        .setPage(pageEntity)
                        .setLemma(lemmaEntity)
                        .setLemmaRank(word.getValue()));
                if (searchIndexEntities.size() >= 5000) {
                    indexRepository.saveAll(searchIndexEntities);
                    searchIndexEntities.clear();
                }
            }
        }
        lemmaRepository.saveAll(lemmaEntities);
        indexRepository.saveAll(searchIndexEntities);
    }

    private Document jsoupConnect(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rvl.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get();
    }

    private PageEntity getPageEntity(String path, SiteEntity site, Document document) {
        return new PageEntity().setSite(site)
                .setPath(path.replaceAll(site.getUrl(), ""))
                .setCode(document.connection().response().statusCode())
                .setContent(document.html());
    }

    private void updateStatusTime(SiteEntity siteEntity) {
        if (siteEntity != null) siteRepository.save(siteEntity.setStatusTime(LocalDateTime.now()));
    }

    private void setStatusFailedAndErrorMessage(SiteEntity siteEntity, String error) {
        if (siteEntity != null) siteRepository.save(siteEntity.setStatus(StatusType.FAILED).setLastError(error));
    }
}
