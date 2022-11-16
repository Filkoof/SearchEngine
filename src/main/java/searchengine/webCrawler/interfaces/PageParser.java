package searchengine.webCrawler.interfaces;

import searchengine.dto.NodePage;

public interface PageParser {

    void parsePage(NodePage nodePage) throws InterruptedException;
}
