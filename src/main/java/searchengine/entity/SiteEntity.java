package searchengine.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import searchengine.entity.enumerated.StatusType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "site")
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private StatusType status;
    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;
}
