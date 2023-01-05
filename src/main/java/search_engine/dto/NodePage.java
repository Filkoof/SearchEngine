package search_engine.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Component
@Getter
@Setter
@Accessors(chain = true)
public class NodePage implements Serializable {

    private String path;
    private String suffix;
    private String prefix;
    private int timeBetweenRequest;
    private int siteId;
    private Set<String> referenceOnChildSet = new HashSet<>();
}
