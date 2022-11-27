package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import search_engine.entity.PageEntity;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer>, Serializable {

    PageEntity findByPath(String path);

    List<PageEntity> findAllBySiteId(int siteId);

    boolean existsByPath(String path);

    boolean existsBySiteId(int siteId);

    int countAllBySiteId(int siteId);

    @Transactional
    @Modifying
    void deleteAllBySiteId(int siteId);
}