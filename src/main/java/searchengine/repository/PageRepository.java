package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    PageEntity findBySite(SiteEntity siteEntity);

    PageEntity findByPath(String path);

    boolean existsByPath(String path);

    void deleteAllBySiteId(long siteId);
}
