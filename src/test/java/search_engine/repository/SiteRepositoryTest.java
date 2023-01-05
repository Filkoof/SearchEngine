package search_engine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import search_engine.ContextLoad;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SiteRepositoryTest extends ContextLoad {

    @Autowired
    private SiteRepository siteRepository;
    private SiteEntity siteEntity;

    @BeforeEach
    void saveTestObjects() {
        siteEntity = new SiteEntity();
        siteEntity.setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl("https://playback.ru")
                .setName("Playback");
        siteRepository.save(siteEntity);
    }

    @Test
    @DisplayName("Найти сущность по id")
    void findSiteById() {
        var site = siteRepository.findById(siteEntity.getId());
        assertNotNull(site);
    }

    @Test
    @DisplayName("Найти все сущности по статусу")
    void findAllByStatus() {
        var sites = siteRepository.findAllByStatus(StatusType.INDEXED);
        assertNotNull(sites);
    }

    @Test
    @DisplayName("Найти по url")
    void findByUrl() {
        var site = siteRepository.findByUrl(siteEntity.getUrl());
        assertNotNull(site);
    }

    @Test
    @DisplayName("Проверка наличия по url")
    void existsByUrl() {
        boolean isExist = siteRepository.existsByUrl(siteEntity.getUrl());
        assertTrue(isExist);
    }

    @Test
    @DisplayName("Удалить сущность по id")
    void deleteSite() {
        siteRepository.deleteById(siteEntity.getId());
        boolean isExist = siteRepository.existsByUrl(siteEntity.getUrl());
        assertFalse(isExist);
    }

    @AfterEach
    void clearDatabase() {
        siteRepository.deleteAll();
    }
}