package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import search_engine.entity.SearchIndexEntity;

@Repository
public interface IndexRepository extends JpaRepository<SearchIndexEntity, Long> {
}
