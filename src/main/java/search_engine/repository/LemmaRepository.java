package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import search_engine.entity.LemmaEntity;

import javax.transaction.Transactional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    boolean existsByLemma(String lemma);

    LemmaEntity findByLemma(String lemma);

    int countAllBySiteId(int siteId);

    @Transactional
    @Modifying
    void deleteAllBySiteId(int siteId);

    @Transactional
    @Modifying
    @Query("UPDATE LemmaEntity l SET l.frequency = l.frequency - 1 WHERE l.lemma = :lemma")
    void decrementAllFrequencyByLemma(String lemma);
}
