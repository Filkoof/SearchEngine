package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.entity.PageEntity;

import javax.transaction.Transactional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    boolean existsByPath(String path);

    boolean existsBySiteId(int siteId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PageEntity p WHERE p.site.id = :siteId")
    void deleteAllBySiteId(int siteId);
}
