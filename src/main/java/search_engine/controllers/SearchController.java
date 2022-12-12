package search_engine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import search_engine.dto.SearchResponse;
import search_engine.services.interfaces.SearchService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam(name = "query", defaultValue = "") String query,
                                                 @RequestParam(name = "site", defaultValue = "") String site,
                                                 @RequestParam(name = "offset", defaultValue = "0") int offset,
                                                 @RequestParam(name = "limit", defaultValue = "10") int limit) throws IOException {
        return ResponseEntity.ok(searchService.search(query, site, offset, limit));
    }
}
