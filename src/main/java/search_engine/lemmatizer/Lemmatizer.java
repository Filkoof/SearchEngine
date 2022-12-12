package search_engine.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import search_engine.dto.LemmaDto;

import java.io.IOException;
import java.util.*;

public class Lemmatizer {

    private final LuceneMorphology luceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[]
            {"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ВВОДН", "ЧАСТ", "МС", "CONJ", "PART"};

    public static Lemmatizer getInstance() throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        return new Lemmatizer(morphology);
    }

    private Lemmatizer(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    public Map<String, Integer> collectLemmas(String text) {
        String[] words = arrayContainsRussianWords(text);
        Map<String, Integer> lemmas = new HashMap<>();

        for (String word : words) {
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (word.isBlank() || anyWordBaseBelongToParticle(wordBaseForms) || normalForms.isEmpty()) continue;

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
            if (!word.isEmpty() && isCorrectWordForm(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) continue;

                lemmaSet.addAll(luceneMorphology.getNormalForms(word));
            }
        }
        return lemmaSet;
    }

    public List<LemmaDto> getLemmaDto(String text) {
        String[] textArray = arrayContainsRussianWords(text);

        List<LemmaDto> lemmas = new LinkedList<>();
        for (String word : textArray) {

            if (!word.isEmpty() && isCorrectWordForm(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) continue;

                var lemmaDto = new LemmaDto()
                        .setIncomingForm(word)
                        .setNormalForm(luceneMorphology.getNormalForms(word).get(0));
                lemmas.add(lemmaDto);
            }
        }
        return lemmas;
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
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
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }
}
