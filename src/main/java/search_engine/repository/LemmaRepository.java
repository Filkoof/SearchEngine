package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import search_engine.entity.LemmaEntity;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
}
