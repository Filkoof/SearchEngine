package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import search_engine.entity.SearchIndexEntity;

import javax.transaction.Transactional;
import java.io.Serializable;

@Repository
public interface IndexRepository extends JpaRepository<SearchIndexEntity, Long>, Serializable {

    boolean existsByPageId(int pageId);

    @Transactional
    @Modifying
    void deleteAllByPageId(int pageId);
}
