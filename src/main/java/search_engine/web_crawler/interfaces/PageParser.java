package search_engine.web_crawler.interfaces;

import search_engine.dto.NodePage;

import java.io.Serializable;

public interface PageParser extends Serializable {

    void startPageParser(NodePage nodePage);

    void parseSinglePage(NodePage nodePage);
}
