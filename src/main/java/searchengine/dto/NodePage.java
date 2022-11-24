package searchengine.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Getter
@Setter
@Accessors(chain = true)
public class NodePage {

    private String path;
    private String suffix;
    private String prefix;
    private int timeBetweenRequest;
    private int siteId;
    private Set<String> referenceOnChildSet = new HashSet<>();
}
