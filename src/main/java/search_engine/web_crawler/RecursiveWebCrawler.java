package search_engine.web_crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import search_engine.dto.NodePage;
import search_engine.entity.enumerated.StatusType;
import search_engine.repository.SiteRepository;
import search_engine.web_crawler.interfaces.PageParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Component
@RequiredArgsConstructor
public class RecursiveWebCrawler extends RecursiveAction {

    private final SiteRepository siteRepository;
    private final PageParser pageParser;
    private final NodePage nodePage;

    @Override
    protected void compute() {
        List<RecursiveWebCrawler> taskList = new ArrayList<>();

        nodePage.setPath(nodePage.getPrefix() + nodePage.getSuffix());

        try {
            pageParser.startPageParser(nodePage);

            for (String refOnChild : nodePage.getReferenceOnChildSet()) {
            NodePage nodePageChild = new NodePage();
            nodePageChild.setPrefix(nodePage.getPrefix())
                    .setSuffix(refOnChild)
                    .setTimeBetweenRequest(nodePage.getTimeBetweenRequest())
                    .setSiteId(nodePage.getSiteId());

            RecursiveWebCrawler task = new RecursiveWebCrawler(siteRepository ,pageParser, nodePageChild);
            Thread.sleep(nodePage.getTimeBetweenRequest());

            task.fork();
            taskList.add(task);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.toString());
        }

        taskList.forEach(ForkJoinTask::join);
        if (isCompleted()) shutdownAndSetStatusIndexed(nodePage.getSiteId());
    }

    private boolean isCompleted() {
        return getPool().getActiveThreadCount() == 1
                && getPool().getQueuedTaskCount() == 0
                && getPool().getQueuedSubmissionCount() == 0;
    }

    private void shutdownAndSetStatusIndexed(int siteId) {
        var site = siteRepository.findById(siteId).orElseThrow();

        getPool().shutdown();
        site.setStatus(StatusType.INDEXED);
        siteRepository.save(site);
    }
}
