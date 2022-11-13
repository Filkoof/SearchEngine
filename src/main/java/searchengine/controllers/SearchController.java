package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

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
