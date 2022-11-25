package search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;

import java.io.Serializable;
import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer>, Serializable {

    List<SiteEntity> findAllByStatus(StatusType status);

    SiteEntity findByUrl(String url);

    boolean existsByUrl(String url);
}
