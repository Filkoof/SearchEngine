package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import search_engine.entity.PageEntity;

import javax.transaction.Transactional;
import java.io.Serializable;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer>, Serializable {

    boolean existsByPath(String path);

    boolean existsBySiteId(int siteId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PageEntity p WHERE p.site.id = :siteId")
    void deleteAllBySiteId(int siteId);
}
