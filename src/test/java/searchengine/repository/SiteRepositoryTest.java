package searchengine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.ContextLoad;
import searchengine.entity.SiteEntity;
import searchengine.entity.enumerated.StatusType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SiteRepositoryTest extends ContextLoad {

    @Autowired
    private SiteRepository siteRepository;
    private SiteEntity siteEntity;

    @BeforeEach
    void savePage() {
        siteEntity = new SiteEntity();
        siteEntity.setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl("https://playback.ru")
                .setName("Playback");
        siteRepository.save(siteEntity);
    }

    @Test
    void findSiteById() {
        var site = siteRepository.findById(siteEntity.getId());
        assertNotNull(site);
    }

    @Test
    void findAllByStatus() {
        var sites = siteRepository.findAllByStatus(StatusType.INDEXED);
        assertNotNull(sites);
    }

    @Test
    void findByUrl() {
        var site = siteRepository.findByUrl(siteEntity.getUrl());
        assertNotNull(site);
    }

    @Test
    void existsByUrl() {
        boolean isExist = siteRepository.existsByUrl(siteEntity.getUrl());
        assertTrue(isExist);
    }

    @Test
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