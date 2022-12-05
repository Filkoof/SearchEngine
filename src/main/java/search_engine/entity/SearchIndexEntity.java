package search_engine.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "search_index")
public class SearchIndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    @JoinColumn(name = "page_id")
    private PageEntity page;
    @OneToOne
    @JoinColumn(name = "lemma_id")
    private LemmaEntity lemma;
    private float lemmaRank;
}