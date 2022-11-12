package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

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

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/search")
    public void search(String query,
                       String site,
                       @RequestParam(name = "offset", defaultValue = "0") int offset,
                       @RequestParam(name = "limit", defaultValue = "20") int limit) {
        /**
         Формат ответа в случае успеха:
         {
         'result': true,
         'count': 574,
         'data': [
         {
         "site": "http://www.site.com",
         "siteName": "Имя сайта",
         "uri": "/path/to/page/6784",
         "title": "Заголовок страницы,
         которую выводим",
         "snippet": "Фрагмент текста,
         в котором найдены
         совпадения, <b>выделенные
         жирным</b>, в формате HTML",
         "relevance": 0.93362
         },
         ...
         ]
         }

         Формат ответа в случае ошибки:
         {
         'result': false,
         'error': "Задан пустой поисковый запрос"
         }
         */
    }
}
