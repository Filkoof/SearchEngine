package searchengine.crawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.entity.PageEntity;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@RequiredArgsConstructor
public class WebCrawler {
    private static List<String> visitedURLs = new ArrayList<>();
    private final PageRepository webPageRepository;

    public void crawl(String rootURL) throws InterruptedException, IOException {
        Thread.sleep(150);

        Document document = jsoupConnect(rootURL);
        Elements elements = document.select("a[href]");

        for (Element element : elements) {
            String path = element.attr("abs:href");

            if (!visitedURLs.contains(path) && path.startsWith("http")) {
                URL url = new URL(path);

                webPageRepository.save(getWebPage(url));

                System.out.println(path + "\t" + jsoupConnect(path));
            }
        }
    }

    public PageEntity getWebPage(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        PageEntity entity = new PageEntity();
        entity.setPath(url.getPath());
        entity.setCode(httpURLConnection.getResponseCode());
        entity.setContent(url.getContent().toString());
        return entity;
    }

    public static Document jsoupConnect(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rvl.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get();
    }
}