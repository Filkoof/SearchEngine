package search_engine.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import search_engine.entity.enumerated.StatusType;

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
    private int id;
    @Column(columnDefinition = "ENUM")
    @Enumerated(EnumType.STRING)
    private StatusType status;
    private LocalDateTime statusTime;
    @Column(columnDefinition = "TEXT)")
    private String lastError;
    private String url;
    private String name;
}
