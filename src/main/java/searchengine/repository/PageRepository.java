package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.PageEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {
}
