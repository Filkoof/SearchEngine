package searchengine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class NodePage {

    private String path;
    private String suffix;
    private String prefix;
    private int timeBetweenRequest;
    private int siteId;
    private Set<String> referenceOnChildSet;

    public NodePage(String path) {
        this.path = path;
        referenceOnChildSet = new HashSet<>();
    }
}
