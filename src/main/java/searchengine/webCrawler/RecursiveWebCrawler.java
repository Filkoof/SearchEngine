package searchengine.webCrawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.NodePage;
import searchengine.webCrawler.interfaces.PageParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Component
@RequiredArgsConstructor
public class RecursiveWebCrawler extends RecursiveAction {

    private final PageParser pageParser;
    private final NodePage nodePage;

    @Override
    protected void compute() {
        List<RecursiveWebCrawler> taskList = new ArrayList<>();

        nodePage.setPath(nodePage.getPrefix() + nodePage.getSuffix());

        try {
            pageParser.parsePage(nodePage);

            for (String refOnChild : nodePage.getReferenceOnChildSet()) {
            NodePage nodePageChild = new NodePage();
            nodePageChild.setPrefix(nodePage.getPrefix())
                    .setSuffix(refOnChild)
                    .setTimeBetweenRequest(nodePage.getTimeBetweenRequest())
                    .setSiteId(nodePage.getSiteId());

            RecursiveWebCrawler task = new RecursiveWebCrawler(pageParser, nodePageChild);
            Thread.sleep(nodePage.getTimeBetweenRequest());

            task.fork();
            taskList.add(task);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        taskList.forEach(ForkJoinTask::join);
    }
}
