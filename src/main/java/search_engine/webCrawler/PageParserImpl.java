package search_engine.webCrawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import search_engine.dto.NodePage;
import search_engine.entity.PageEntity;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;
import search_engine.repository.PageRepository;
import search_engine.repository.SiteRepository;
import search_engine.webCrawler.interfaces.PageParser;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
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
                var pageEntity = getPageEntity(subPath, siteEntity, document);
                var referenceOnChildSet = nodePage.getReferenceOnChildSet();

                if (isNeedSave(subPath, siteEntity)) {
                    pageRepository.save(pageEntity);
                    referenceOnChildSet.add(subPath);
                }
            }
        } catch (IOException e) {
            setStatusFailedAndErrorMessage(siteEntity, e.toString());
            throw new RuntimeException(e);
        }
    }

    private Document jsoupConnect(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rvl.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get();
    }

    private boolean isNeedSave(String path, SiteEntity site) {
        return !pageRepository.existsByPath(path)
                && path.startsWith(site.getUrl())
                && !path.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf|xml))$)")
                && !path.contains("#");
    }

    private PageEntity getPageEntity(String path, SiteEntity site, Document document) {
        return new PageEntity().setSite(site)
                .setPath(path)
                .setCode(document.connection().response().statusCode())
                .setContent(document.html());
    }

    private void updateStatusTime(SiteEntity siteEntity) {
        if (siteEntity != null) siteRepository.save(siteEntity.setStatusTime(LocalDateTime.now()));
    }

    private void setStatusFailedAndErrorMessage(SiteEntity siteEntity, String error) {
        if (siteEntity != null) siteRepository.save(siteEntity.setStatus(StatusType.FAILED).setLastError(error));
    }
}
