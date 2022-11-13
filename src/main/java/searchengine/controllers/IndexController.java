package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexController {

    @GetMapping("/startIndexing")
    public void startIndexing() {
        /**
         Формат ответа в случае успеха:
         {
         'result': true
         }

         Формат ответа в случае ошибки:
         {
         'result': false,
         'error': "Индексация уже запущена"
         }
         */
    }

    @GetMapping("/stopIndexing")
    public void stopIndexing() {
        /**
         Формат ответа в случае успеха:

         {
         'result': true
         }

         Формат ответа в случае ошибки:

         {
         'result': false,
         'error': "Индексация не запущена"
         }
         */
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
