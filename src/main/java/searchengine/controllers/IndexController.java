package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.IndexResponse;
import searchengine.services.IndexServiceImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexController {

    private final IndexServiceImpl indexService;

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        return ResponseEntity.ok(indexService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public void indexPage(@RequestParam(name = "url", defaultValue = "") String url) {
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
