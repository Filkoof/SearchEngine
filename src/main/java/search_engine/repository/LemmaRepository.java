package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import search_engine.entity.LemmaEntity;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long>, Serializable {
    boolean existsBySiteIdAndLemma(int siteId, String lemma);

    Optional<LemmaEntity> findBySiteIdAndLemma(int siteId, String lemma);

    @Query(value = """
                  SELECT l.frequency / count(p.id) as percent
                  FROM lemmas as l
                  JOIN page as p ON l.site_id = p.site_id
                  WHERE l.id = :id
                  """, nativeQuery = true)
    double percentageLemmaOnPagesById(int id);

    @Query(value = """
                  SELECT max(percentage_lemma) FROM
                  (
                  SELECT l.frequency / count(p.id) as percentage_lemma
                  FROM lemmas as l
                  JOIN page as p ON l.site_id = p.site_id
                  WHERE l.site_id = :siteId
                  GROUP BY l.id
                  ) as percentage_lemmas_on_page
                  """, nativeQuery = true)
    double findMaxPercentageLemmaOnPagesBySiteId(int siteId);

    int countAllBySiteId(int siteId);

    @Transactional
    @Modifying
    void deleteAllBySiteId(int siteId);


    @Transactional
    @Modifying
    @Query("UPDATE LemmaEntity l SET l.frequency = l.frequency - 1 WHERE l.site.id = :siteId AND l.lemma = :lemma")
    void decrementAllFrequencyBySiteIdAndLemma(int siteId, String lemma);
}
