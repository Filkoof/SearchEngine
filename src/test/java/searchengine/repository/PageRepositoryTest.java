package searchengine.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.ContextLoad;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.enumerated.StatusType;

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
    void findPageById() {
        var page = pageRepository.findById(pageEntity.getId()).orElseThrow(EntityNotFoundException::new);
        assertNotNull(page);
    }

    @Test
    void existByPath() {
        boolean isExist = pageRepository.existsByPath(pageEntity.getPath());
        assertTrue(isExist);
    }

    @Test
    void existsBySiteId() {
        boolean isExist = pageRepository.existsBySiteId(siteEntity.getId());
        assertTrue(isExist);
    }

    @Test
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