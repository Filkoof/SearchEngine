package search_engine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ContextLoad {

    @MockBean
    private LuceneMorphology luceneMorphology;
}
