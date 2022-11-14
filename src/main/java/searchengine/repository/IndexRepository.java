package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.SearchIndexEntity;

@Repository
public interface IndexRepository extends JpaRepository<SearchIndexEntity, Long> {
}
