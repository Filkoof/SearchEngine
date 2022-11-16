package searchengine.entity;

import javax.persistence.*;

import com.mysql.cj.protocol.ColumnDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "page")
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "site_id")
    private SiteEntity site;
    private String path;
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
}
