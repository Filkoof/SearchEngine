package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.ResponseDto;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.enumerated.StatusType;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.interfaces.IndexService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sites;

    /*
     * TODO:
     *  1. Реализовать метод stopIndexing
     *  2. Протестировать!
     * */

    public ResponseDto<Map<String, Boolean>> stopIndexing() {

        return ResponseDto.<Map<String, Boolean>>builder()
                .data(Map.of("result", true))
                .error(true ? "Индексация не запущена" : "")
                .timeStamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ResponseDto<Map<String, Boolean>> indexing() throws IOException, InterruptedException {
        var sitesList = sites.getSites();

        sitesList.parallelStream().forEach(site -> {
            try {
                deleteAllInfoFromDataBase(site);
                saveSiteInDataBase(site);
                webCrawl(site.getUrl());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        for(Site site : sitesList) {
            var status = siteRepository.findByUrl(site.getUrl()).getStatus();
            return ResponseDto.<Map<String, Boolean>>builder()
                    .data(Map.of("result", !status.equals(StatusType.INDEXING)))
                    .error(status.equals(StatusType.INDEXING) ? "Индексация  уже запущена" : "")
                    .timeStamp(LocalDateTime.now())
                    .build();
        }

        return null;
    }

    private void webCrawl(String path) throws InterruptedException {
        Thread.sleep(150);

        var isPageExist = pageRepository.existsByPath(path);
        var siteEntity = isPageExist ? pageRepository.findByPath(path).getSite() : siteRepository.findByUrl(path);

        Document document;
        try {
            document = jsoupConnect(path);
            var elements = document.select("a[href]");

            for (Element element : elements) {
                updateStatusTime(siteEntity);

                var subPath = element.attr("abs:href");
                if (isNeedSave(subPath, siteEntity.getUrl())) savePageAndStartWebCrawl(subPath, siteEntity);
            }

        } catch (IOException e) {
            setStatusFailedAndErrorMessage(siteEntity, e.toString());
            throw new RuntimeException(e);
        }

        setStatusIndexed(siteEntity);
    }

    private void savePageAndStartWebCrawl(String url, SiteEntity siteEntity) throws IOException, InterruptedException {
        pageRepository.save(getWebPage(url, siteEntity));
        webCrawl(url);
    }

    private void deleteAllInfoFromDataBase(Site site) {
        var siteForDelete = siteRepository.findByUrl(site.getUrl());
        siteRepository.deleteById(siteForDelete.getId());
        pageRepository.deleteAllBySiteId(siteForDelete.getId());
    }

    private void saveSiteInDataBase(Site site) {
        var siteEntity = new SiteEntity();
        siteEntity.setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError(null)
                .setUrl(site.getUrl())
                .setName(site.getName());
        siteRepository.save(siteEntity);
    }

    private boolean isNeedSave(String path, String siteUrl) {
        return !pageRepository.existsByPath(path)
                && path.contains(siteUrl)
                && path.startsWith("http")
                && !path.contains("#")
                && !path.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf|xml))$)")
                && !path.matches("(instagram|twitter|facebook|vkontakte)")
                && !path.matches("#([\\w\\-]+)?$")
                && !path.contains("?method=");
    }

    private PageEntity getWebPage(String path, SiteEntity siteEntity) throws IOException {
        var url = new URL(path);
        var httpURLConnection = (HttpURLConnection) url.openConnection();

        var pageEntity = new PageEntity();
        return pageEntity.setSite(siteEntity)
                .setPath(url.getPath())
                .setCode(httpURLConnection.getResponseCode())
                .setContent(url.getContent().toString());
    }

    private Document jsoupConnect(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rvl.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get();
    }

    private void updateStatusTime(SiteEntity siteEntity) {
        siteRepository.save(siteEntity.setStatusTime(LocalDateTime.now()));
    }

    private void setStatusFailedAndErrorMessage(SiteEntity siteEntity, String error) {
        siteRepository.save(siteEntity.setStatus(StatusType.FAILED).setLastError(error));
    }

    private void setStatusIndexed(SiteEntity siteEntity) {
        siteRepository.save(siteEntity.setStatus(StatusType.INDEXED));
    }
}