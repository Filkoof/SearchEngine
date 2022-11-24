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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        for (String refOnChild : nodePage.getReferenceOnChildSet()) {
            NodePage nodePageChild = new NodePage();
            nodePageChild.setPrefix(nodePage.getPrefix())
                    .setSuffix(refOnChild)
                    .setTimeBetweenRequest(nodePage.getTimeBetweenRequest())
                    .setSiteId(nodePage.getSiteId());

            RecursiveWebCrawler task = new RecursiveWebCrawler(pageParser, nodePageChild);

            try {
                Thread.sleep(nodePage.getTimeBetweenRequest());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            task.fork();
            taskList.add(task);
        }

        taskList.forEach(ForkJoinTask::join);
    }
}
