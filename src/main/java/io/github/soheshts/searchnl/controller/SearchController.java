package io.github.soheshts.searchnl.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @PostMapping("send")
    public String processRequest(@RequestBody final String body){
        return "Hello there";
    }
}
