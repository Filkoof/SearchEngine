package search_engine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import search_engine.ContextLoad;
import search_engine.entity.LemmaEntity;
import search_engine.entity.PageEntity;
import search_engine.entity.SearchIndexEntity;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IndexRepositoryTest extends ContextLoad {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private PageEntity pageEntity;

    @BeforeEach
    void addTestObjects() {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl("https://playback.ru")
                .setName("Playback");
        siteRepository.save(siteEntity);

        pageEntity = new PageEntity();
        pageEntity.setSite(siteEntity)
                .setPath("https://playback.ru")
                .setCode(200)
                .setContent("content");
        pageRepository.save(pageEntity);

        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setSite(siteEntity)
                .setLemma("кот")
                .setFrequency(2);
        lemmaRepository.save(lemmaEntity);

        SearchIndexEntity searchIndexEntity = new SearchIndexEntity();
        searchIndexEntity.setPage(pageEntity)
                .setLemma(lemmaEntity)
                .setLemmaRank(5);
        indexRepository.save(searchIndexEntity);
    }

    @Test
    @DisplayName("Проверка наличия по id страницы")
    void existsByPageId() {
        boolean isExist = indexRepository.existsByPageId(pageEntity.getId());
        assertTrue(isExist);
    }

    @Test
    @DisplayName("Удалить все по id сайта")
    void deleteAllByPageId() {
        indexRepository.deleteAllByPageId(pageEntity.getId());
        boolean isExist = indexRepository.existsByPageId(pageEntity.getId());
        assertFalse(isExist);
    }

    @AfterEach
    void clearDatabase() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }
}