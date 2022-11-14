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
    *  1. Реализовать рекурсивный парсинг
    *  2. Реализовать многопоточный парсинг
    *  3. Придумать как получать корневой сайт в getWebPage
    *  4. В indexing подумать как получать информацию об ошибке для return
    *  5. Реализовать метод stopIndexing
     * */

    @Override
    public ResponseDto<Map<String, Boolean>> indexing() throws IOException, InterruptedException {
        var sitesList = sites.getSites();

        for (Site site : sitesList) {
            deleteSiteFromDataBase(site);
            saveSiteInDataBase(site);
            webCrawl(site.getUrl());
        }

        return ResponseDto.<Map<String, Boolean>>builder()
                .data(Map.of("result", true)) // false в случае ошибки
                .error("") //'result': false ? 'error': "Индексация не запущена"
                .timeStamp(LocalDateTime.now())
                .build();
    }

    private void webCrawl(String rootURL) throws InterruptedException {
        Thread.sleep(150);

        var siteEntity = siteRepository.findByUrl(rootURL);

        Document document;
        try {
            document = jsoupConnect(rootURL);
            var elements = document.select("a[href]");

            for (Element element : elements) {
                updateStatusTime(siteEntity);

                var url = element.attr("abs:href");
                if (isNeedSave(url)) pageRepository.save(getWebPage(url, siteEntity));
            }
        } catch (IOException e) {
            setStatusFailedAndErrorMessage(siteEntity, e.toString());
            throw new RuntimeException(e);
        }

        setStatusIndexed(siteEntity);
    }

    private void deleteSiteFromDataBase(Site site) {
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

    private boolean isNeedSave(String path) {
        return !pageRepository.existsByPath(path)
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