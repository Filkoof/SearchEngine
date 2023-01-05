package search_engine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import search_engine.dto.IndexResponse;
import search_engine.services.IndexServiceImpl;

import java.io.IOException;

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
    public ResponseEntity<IndexResponse> indexPage(@RequestParam(name = "url", defaultValue = "") String url) throws IOException {
        return ResponseEntity.ok(indexService.indexPage(url));
    }
}
