package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.IndexEntity;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {
}
