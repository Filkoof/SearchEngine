package search_engine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import search_engine.ContextLoad;
import search_engine.entity.LemmaEntity;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LemmaRepositoryTest extends ContextLoad {

    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private SiteRepository siteRepository;
    private SiteEntity siteEntity;
    private LemmaEntity lemmaEntity;

    @BeforeEach
    void addTestObjects() {
        siteEntity = new SiteEntity();
        siteEntity.setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl("https://playback.ru")
                .setName("Playback");
        siteRepository.save(siteEntity);

        lemmaEntity = new LemmaEntity();
        lemmaEntity.setSite(siteEntity)
                .setLemma("кот")
                .setFrequency(2);
        lemmaRepository.save(lemmaEntity);
    }

    @Test
    @DisplayName("Проверка наличия по лемме")
    void existsByLemma() {
        boolean isExist = lemmaRepository.existsByLemma(lemmaEntity.getLemma());
        assertTrue(isExist);
    }

    @Test
    @DisplayName("Найти сущность по лемме")
    void findByLemma() {
        var lemma = lemmaRepository.findByLemma(lemmaEntity.getLemma());
        assertNotNull(lemma);
    }

    @Test
    @DisplayName("Удалить всё по id сайта")
    void countAllBySiteId() {
        int lemmasCount = lemmaRepository.countAllBySiteId(siteEntity.getId());
        assertEquals(1, lemmasCount);
    }

    @Test
    @DisplayName("Удалить всё по id сайта")
    void deleteAllBySiteId() {
        lemmaRepository.deleteAllBySiteId(siteEntity.getId());
        boolean isExist = lemmaRepository.existsByLemma(lemmaEntity.getLemma());
        assertFalse(isExist);
    }

    @Test
    @DisplayName("Декремент значения frequency по лемме")
    void decrementAllFrequencyByLemma() {
        lemmaRepository.decrementAllFrequencyByLemma(lemmaEntity.getLemma());
        var lemma = lemmaRepository.findByLemma(lemmaEntity.getLemma());
        assertEquals(1, lemma.getFrequency());
    }

    @AfterEach
    void clearDatabase() {
        lemmaRepository.deleteAll();
        siteRepository.deleteAll();
    }
}