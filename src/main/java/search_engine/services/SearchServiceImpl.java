package search_engine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import search_engine.annotation.Profiling;
import search_engine.config.SitesList;
import search_engine.dto.SearchDto;
import search_engine.dto.SearchResponse;
import search_engine.entity.LemmaEntity;
import search_engine.entity.PageEntity;
import search_engine.entity.SiteEntity;
import search_engine.lemmatizer.Lemmatizer;
import search_engine.repository.IndexRepository;
import search_engine.repository.LemmaRepository;
import search_engine.repository.PageRepository;
import search_engine.repository.SiteRepository;
import search_engine.services.interfaces.SearchService;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Profiling(enabled = false)
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sites;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {
        if (query.isEmpty()) return new SearchResponse().setResult(false).setError("Задан пустой поисковый запрос");
        Pageable pageable = PageRequest.of(offset / limit, limit);

        var data = site.isEmpty() ? searchAllSites(query, pageable) : searchSite(query, site, pageable);
        return new SearchResponse()
                .setResult(true)
                .setCount(data.size())
                .setData(data)
                .setError("");
    }

    private List<SearchDto> searchAllSites(String query, Pageable pageable) {
        var siteList= sites.getSites();
        List<SearchDto> data = new ArrayList<>();
        siteList.forEach(site -> data.addAll(searchSite(query, site.getUrl(), pageable)));
        return data;
    }

    private List<SearchDto> searchSite(String query, String siteUrl, Pageable pageable) {
        var site = siteRepository.findByUrl(siteUrl);
        var filteredLemmas = getFrequencyFilteredLemmas(query, site);

        Page<PageEntity> pages = pageRepository.findAllByLemmas(filteredLemmas, pageable);

        double maxRelevance = pages.stream().map(page -> indexRepository.absoluteRelevanceByPageId(page.getId())).max(Double::compareTo).orElse(0.45);
        List<SearchDto> data = new ArrayList<>();
        for (PageEntity page : pages) {
            var content = page.getContent();
            var title = getTitleFromContent(content);
            var snippet = getSnippet(content ,filteredLemmas);
            var relativeRelevance = calculateRelativeRelevance(page.getId(), maxRelevance);

            data.add(new SearchDto()
                    .setSite(site.getUrl())
                    .setSiteName(site.getName())
                    .setUri(page.getPath())
                    .setTitle(title)
                    .setSnippet(snippet)
                    .setRelevance(relativeRelevance));
        }

        return data;
    }

    private String getSnippet(String content, List<LemmaEntity> queryLemmas) {
        long before = System.nanoTime();

        int countMatches = 0;
        Map<String, Integer> snippets = new HashMap<>();
        for (LemmaEntity lemmaEntity : queryLemmas) {
            countMatches++;

            String lemma = deleteWordEnding(lemmaEntity.getLemma());

            String regex = "[\\s*()A-Za-zА-Яа-я-,\\d/]*";
            Pattern pattern = Pattern.compile(regex.concat(lemma).concat(regex));
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String match = matcher.group();
                snippets.put(match, countMatches);
            }
        }

        if (snippets.isEmpty()) return "";

        var snippet = Objects.requireNonNull(snippets.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null)).getKey();
        for (LemmaEntity lemmaEntity : queryLemmas) {
            String lemma = deleteWordEnding(lemmaEntity.getLemma());

            Pattern pattern = Pattern.compile(lemma.concat("[A-Za-zА-Яа-я]{2,4}?"));
            Matcher matcher = pattern.matcher(snippet);

            while (matcher.find()) {
                String match = matcher.group();
                snippet = snippet.replaceAll(match, "<b>".concat(match).concat("</b>"));
            }
        }

        long after = System.nanoTime();
        System.out.println("Время получения сниппета: ".concat(String.valueOf(after - before)));
        return "snippet";
    }

    private String deleteWordEnding(String word) {
        return word.length() <= 3 ? word : word.substring(0, word.length() - 2);
    }

    private String getTitleFromContent(String content) {
        int beginIndex = content.indexOf("<title>");
        int endIndex = content.indexOf("</title>");
        return content.substring(beginIndex + 7, endIndex);
    }

    private double calculateRelativeRelevance(int pageId, double maxRelevance) {
        double absRelevance = indexRepository.absoluteRelevanceByPageId(pageId);
        return absRelevance / maxRelevance;
    }


    /*
    Фильтрация из запроса лемм, которые встречаются на слишком большом количестве страниц.
    Если фильтр не нашел редких лемм, возвращаем все леммы из запроса
     */
    private List<LemmaEntity> getFrequencyFilteredLemmas(String query, SiteEntity site) {
        double frequencyLimit = getFrequencyLimit(site);

        var lemmas = getLemmatizer().getLemmaSet(query);

        var lemmaEntityList = lemmas.stream().map(lemma -> lemmaRepository.findBySiteIdAndLemma(site.getId(), lemma).orElse(null))
                .filter(Objects::nonNull).toList();
        var filterFrequency = lemmaEntityList.stream().filter(lemma -> lemmaRepository.percentageLemmaOnPagesById(lemma.getId()) < frequencyLimit).toList();

        return filterFrequency.isEmpty() ?
                lemmaEntityList.stream().sorted(Comparator.comparing(LemmaEntity::getFrequency)).toList()
                :
                filterFrequency.stream().sorted(Comparator.comparing(LemmaEntity::getFrequency)).toList();
    }

    @Cacheable("frequencyLimit")
    public double getFrequencyLimit(SiteEntity site) {
        double maxPercentLemmaOnPage = lemmaRepository.findMaxPercentageLemmaOnPagesBySiteId(site.getId());
        double maxFrequencyPercentage = 0.75;
        return maxPercentLemmaOnPage * maxFrequencyPercentage;
    }

    private Lemmatizer getLemmatizer() {
        try {
            return Lemmatizer.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
