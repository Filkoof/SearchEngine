package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.SiteEntity;
import searchengine.entity.enumerated.StatusType;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    List<SiteEntity> findAllByStatus(StatusType status);

    SiteEntity findByUrl(String url);

    boolean existsByUrl(String url);
}
