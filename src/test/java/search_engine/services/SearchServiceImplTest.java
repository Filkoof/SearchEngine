package search_engine.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import search_engine.ContextLoad;
import search_engine.entity.LemmaEntity;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.services.interfaces.SearchService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchServiceImplTest extends ContextLoad {

    @Autowired
    private SearchService searchService;
    private SiteEntity site;
    private LemmaEntity lemmaFirst;
    private LemmaEntity lemmaSecond;

    @BeforeEach
    void saveTestObjects() {
        site = new SiteEntity();
        site.setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl("https://playback.ru")
                .setName("Playback");

        lemmaFirst = new LemmaEntity();
        lemmaFirst.setId(1)
                .setLemma("синий")
                .setSite(site)
                .setFrequency(1);
        lemmaSecond = new LemmaEntity();
        lemmaSecond.setId(2)
                .setLemma("характеристики")
                .setSite(site)
                .setFrequency(1);
    }

    @Test
    void getSnippetTest() throws Exception {
        var searchClass = Class.forName("search_engine.services.SearchServiceImpl");
        var getSnippet = searchClass.getDeclaredMethod("getSnippet", String.class, List.class);

        getSnippet.setAccessible(true);

        List<LemmaEntity> lemmaEntities = new ArrayList<>();
        lemmaEntities.add(lemmaFirst);
        lemmaEntities.add(lemmaSecond);

        String content = Files.readAllLines(Paths.get("src/test/resources/contentText")).toString();

        var getSnippetOutput = getSnippet.invoke(searchService, content, lemmaEntities).toString();

        assertEquals("Описание и <b>характеристики</b> Смартфон Xiaomi Redmi 10A 3/64 ГБ Global, <b>синий</b>"
                , getSnippetOutput);
    }
 }
