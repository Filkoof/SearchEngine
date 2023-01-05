package search_engine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import search_engine.entity.PageEntity;

import javax.transaction.Transactional;
import java.io.Serializable;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer>, Serializable {

    PageEntity findByPath(String path);

    @Query("""
           SELECT p FROM PageEntity p
           JOIN SearchIndexEntity s ON p.id = s.page.id
           WHERE s.lemma.id = :lemmaId
           """)
    Page<PageEntity> findAllByLemmaId(int lemmaId, Pageable pageable);

    boolean existsByPath(String path);

    int countAllBySiteId(int siteId);

    @Transactional
    @Modifying
    void deleteAllBySiteId(int siteId);
}
