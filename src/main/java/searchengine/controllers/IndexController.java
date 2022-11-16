package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.ResponseDto;
import searchengine.services.IndexServiceImpl;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexController {

    private final IndexServiceImpl indexService;

    @GetMapping("/startIndexing")
    public ResponseDto<Map<String, Boolean>> startIndexing() throws IOException, InterruptedException {
        return indexService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public ResponseDto<Map<String, Boolean>> stopIndexing() {
        return indexService.stopIndexing();
            }

    @PostMapping("/indexPage")
    public void indexPage (@RequestParam(name = "url", defaultValue = "") String url) {
        /**
         Формат ответа в случае успеха:
         {
         'result': true
         }

         Формат ответа в случае ошибки:

         {
         'result': false,
         'error': "Данная страница находится за пределами сайтов,
         указанных в конфигурационном файле"
         }

         */
    }
}
