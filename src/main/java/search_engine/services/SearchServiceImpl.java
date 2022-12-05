package search_engine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import search_engine.config.SitesList;
import search_engine.dto.LemmaDto;
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

        var data = site.isEmpty() ? searchAllSites(query) : searchSite(query, site);
        return new SearchResponse()
                .setResult(true)
                .setCount(data.size())
                .setData(data)
                .setError("");
    }

    private List<SearchDto> searchAllSites(String query) {
        var siteList= sites.getSites();
        List<SearchDto> data = new ArrayList<>();
        siteList.forEach(site -> data.addAll(searchSite(query, site.getUrl())));
        return data;
    }

    private List<SearchDto> searchSite(String query, String siteUrl) {
        var site = siteRepository.findByUrl(siteUrl);
        var filteredLemmas = getFrequencyFilteredLemmas(query, site);

        Set<PageEntity> pages = new HashSet<>();
        for (LemmaEntity lemma : filteredLemmas) {
            var pageEntities = pageRepository.findAllByLemmaId(lemma.getId());
            if (pages.isEmpty()) pages.addAll(pageEntities);
            pages.retainAll(pageEntities);
        }

        double maxRelevance = pages.stream().map(page -> indexRepository.absoluteRelevanceByPageId(page.getId())).max(Double::compareTo).orElseThrow();
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
        List<String> words = Arrays.stream(content.split("\\s+")).toList();
        var lemmaDtoSet = getLemmas(content);

        String snippet = "";
        for (LemmaDto lemmaDto : lemmaDtoSet) {
            for (LemmaEntity lemmaEntity : queryLemmas) {
                if (lemmaDto.getNormalForm().equals(lemmaEntity.getLemma())) {
                    words.indexOf(lemmaDto.getIncomingForm());

                    String regex = "[?\\sa-zA-ZА-Яа-я,-.\\d]+";
                    Pattern pattern = Pattern.compile(regex + lemmaDto.getIncomingForm() + regex);
                    Matcher matcher = pattern.matcher(content);
                    String result = matcher.group();
                    result.replaceAll(lemmaDto.getIncomingForm(), "<b>" + lemmaDto.getIncomingForm() + "</b>");
                }
            }
        }

        return snippet;
    }

    private String getTitleFromContent(String content) {
        int beginIndex = content.indexOf("<title>");
        int endIndex = content.indexOf("</title>");
        return content.substring(beginIndex, endIndex + 8);
    }

    private double calculateRelativeRelevance(int pageId, double maxRelevance) {
        double absRelevance = indexRepository.absoluteRelevanceByPageId(pageId);
        return absRelevance / maxRelevance;
    }

    private List<LemmaEntity> getFrequencyFilteredLemmas(String query, SiteEntity site) {
        double maxPercentLemmaOnPage = lemmaRepository.findMaxPercentageLemmaOnPagesBySiteId(site.getId());
        double maxFrequencyPercentage = 0.75;
        double frequencyLimit = maxPercentLemmaOnPage * maxFrequencyPercentage;
        var lemmas = getLemmas(query);
        var lemmaEntityList = lemmas.stream().map(lemma -> lemmaRepository.findBySiteIdAndLemma(site.getId(), lemma.getNormalForm()).orElse(null)).toList();
        var filterNull = lemmaEntityList.stream().filter(Objects::nonNull).toList();
        var filterFrequency = filterNull.stream().filter(lemma -> lemmaRepository.percentageLemmaOnPagesById(lemma.getId()) < frequencyLimit).toList();
        var sort = filterFrequency.stream().sorted(Comparator.comparing(LemmaEntity::getFrequency)).toList();

        return filterFrequency.isEmpty() ?
                filterNull.stream().sorted(Comparator.comparing(LemmaEntity::getFrequency)).toList()
                :
                sort;
    }

    private Set<LemmaDto> getLemmas(String text) {
        try {
            Lemmatizer lemmatizer = Lemmatizer.getInstance();
            return lemmatizer.getLemmaDtoSet(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
