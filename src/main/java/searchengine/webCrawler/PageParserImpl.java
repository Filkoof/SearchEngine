package searchengine.webCrawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchengine.dto.NodePage;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.enumerated.StatusType;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.webCrawler.interfaces.PageParser;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PageParserImpl implements PageParser {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;


    @Override
    public void parsePage(NodePage nodePage) {
        var siteEntity = siteRepository.findById(nodePage.getSiteId()).orElseThrow(EntityNotFoundException::new);

        Document document;
        try {
            document = jsoupConnect(nodePage.getPath());
            var elements = document.select("a[href]");

            for (Element element : elements) {
                updateStatusTime(siteEntity);
                var subPath = element.attr("abs:href");

                if (isNeedSave(subPath) && siteEntity != null) pageRepository.save(getPageEntity(subPath, siteEntity));
            }
        } catch (IOException e) {
            setStatusFailedAndErrorMessage(siteEntity, e.toString());
            throw new RuntimeException(e);
        }
        setStatusIndexed(siteEntity);
    }

    private Document jsoupConnect(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rvl.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get();
    }

    private boolean isNeedSave(String path) {
        return !pageRepository.existsByPath(path)
                && path.startsWith("http")
                && !path.contains("#")
                && !path.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf|xml))$)")
                && !path.matches("(instagram|twitter|facebook|vkontakte)");
//                && !path.matches("#([\\w\\-]+)?$")
//                && !path.contains("?method=");
    }

    private PageEntity getPageEntity(String path, SiteEntity siteEntity) {
        URL url;
        try {
            url = new URL(path);

            var httpURLConnection = (HttpURLConnection) url.openConnection();

            var pageEntity = new PageEntity();
            return pageEntity.setSite(siteEntity)
                    .setPath(path)
                    .setCode(httpURLConnection.getResponseCode())
                    .setContent(url.getContent().toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateStatusTime(SiteEntity siteEntity) {
        if (siteEntity != null) siteRepository.save(siteEntity.setStatusTime(LocalDateTime.now()));
    }

    private void setStatusFailedAndErrorMessage(SiteEntity siteEntity, String error) {
        if (siteEntity != null) siteRepository.save(siteEntity.setStatus(StatusType.FAILED).setLastError(error));
    }


    private void setStatusIndexed(SiteEntity siteEntity) {
        if (siteEntity != null) siteRepository.save(siteEntity.setStatus(StatusType.INDEXED));
    }
}
