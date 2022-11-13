package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.entity.PageEntity;
import searchengine.repository.PageRepository;
import searchengine.services.interfaces.WebCrawlerService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@RequiredArgsConstructor
public class WebCrawlerServiceImpl implements WebCrawlerService {
    private final PageRepository pageRepository;

    @Override
    public void crawl(String rootURL) throws InterruptedException, IOException {
        Thread.sleep(150);

        Document document = jsoupConnect(rootURL);
        Elements elements = document.select("a[href]");

        for (Element element : elements) {
            String path = element.attr("abs:href");

            if (isNeedSave(path)) {
                pageRepository.save(getWebPage(path));

                System.out.println(path + "\t" + jsoupConnect(path));
            }
        }
    }

    private boolean isNeedSave(String path) {
        return !pageRepository.existsByPath(path) && path.startsWith("http");
    }

    private PageEntity getWebPage(String path) throws IOException {
        URL url = new URL(path);
        var httpURLConnection = (HttpURLConnection) url.openConnection();

        var pageEntity = new PageEntity();
        pageEntity.setPath(url.getPath())
                .setCode(httpURLConnection.getResponseCode())
                .setContent(url.getContent().toString());
        return pageEntity;
    }

    private Document jsoupConnect(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rvl.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get();
    }
}