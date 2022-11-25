package search_engine.webCrawler.interfaces;

import search_engine.dto.NodePage;

import java.io.Serializable;

public interface PageParser extends Serializable {

    void parsePage(NodePage nodePage) throws InterruptedException;
}
