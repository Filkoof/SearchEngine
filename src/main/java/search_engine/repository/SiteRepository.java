package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer>, Serializable {

    Optional<List<SiteEntity>> findAllByStatus(StatusType status);

    SiteEntity findByUrl(String url);

    boolean existsByUrl(String url);

    boolean existsByStatus(StatusType status);
}
