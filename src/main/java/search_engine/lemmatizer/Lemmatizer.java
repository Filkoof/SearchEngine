package search_engine.lemmatizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Lemmatizer {
    private static final String WORDS_TYPE = "\\W\\w&&[^а-яА-Я\\s]";
    private final List<String> particlesNames = List.of("МЕЖД", "ПРЕДЛ", "СОЮЗ");
    private final LuceneMorphology luceneMorphology;

    public Map<String, Integer> collectLemmas(String text) {
        String[] words = arrayContainsRussianWords(text);
        Map<String, Integer> lemmas = new HashMap<>();

        for (String word : words) {
            if (word.isBlank()) continue;

            var wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) continue;

            var normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) continue;

            String normalWord = normalForms.get(0);
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }

        return lemmas;
    }

    public Set<String> getLemmaSet(String text) {
        String[] textArray = arrayContainsRussianWords(text);
        Set<String> lemmaSet = new HashSet<>();

        for (String word : textArray) {
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (isNeedAddToLemmaSet(word, wordBaseForms)) lemmaSet.addAll(luceneMorphology.getNormalForms(word));
        }

        return lemmaSet;
    }

    private boolean isNeedAddToLemmaSet(String word, List<String> wordBaseForms) {
        return !word.isEmpty() && isCorrectWordForm(word) && !anyWordBaseBelongToParticle(wordBaseForms);
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::isThisParticle);
    }

    private boolean isThisParticle(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean isCorrectWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);

        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORDS_TYPE)) return false;
        }

        return true;
    }
}
