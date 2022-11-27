package search_engine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import search_engine.ContextLoad;
import search_engine.entity.PageEntity;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PageRepositoryTest extends ContextLoad {

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SiteRepository siteRepository;
    private SiteEntity siteEntity;
    private PageEntity pageEntity;

    @BeforeEach
    void addTestObjects() {
        siteEntity = new SiteEntity();
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
    }

    @Test
    @DisplayName("Найти сущность по id")
    void findPageById() {
        var page = pageRepository.findById(pageEntity.getId()).orElseThrow(EntityNotFoundException::new);
        assertNotNull(page);
    }

    @Test
    @DisplayName("Проверка наличия по пути страницы")
    void existByPath() {
        boolean isExist = pageRepository.existsByPath(pageEntity.getPath());
        assertTrue(isExist);
    }

    @Test
    @DisplayName("Проверка наличия по id сайта")
    void existsBySiteId() {
        boolean isExist = pageRepository.existsBySiteId(siteEntity.getId());
        assertTrue(isExist);
    }

    @Test
    @DisplayName("Количество всех сущностей по id сайта")
    void countAllBySiteId() {
        int pageCount = pageRepository.countAllBySiteId(siteEntity.getId());
        assertEquals(1, pageCount);
    }

    @Test
    @DisplayName("Удалить сущность")
    void deletePage() {
        pageRepository.delete(pageEntity);
        boolean isExist = pageRepository.existsById(pageEntity.getId());
        assertFalse(isExist);
    }

    @AfterEach
    void clearDatabase() {
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }
}