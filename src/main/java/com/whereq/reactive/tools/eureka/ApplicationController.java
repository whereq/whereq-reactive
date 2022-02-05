package com.whereq.reactive.tools.eureka;

import com.whereq.reactive.tools.shared.ApplicationInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/application")
public class ApplicationController {

    @Autowired
    ApplicationService applicationService;

    @GetMapping("all")
    public Mono<Page<ApplicationInstance>> getAll(@RequestParam("page") int page, @RequestParam("size") int size) {
        return this.applicationService.getApplicationInstance(PageRequest.of(page, size));
    }


    @RequestMapping(method = RequestMethod.POST)
    public Mono<Page<ApplicationInstance>> query(@RequestParam("page") int page, @RequestParam("size") int size,
                                                 @RequestBody ApplicationInstance applicationInstance) {
        return this.applicationService.getApplicationInstance(PageRequest.of(page, size), applicationInstance);
    }

    @PostMapping("/instances")
    public Flux<ApplicationInstance> queryBy(@RequestParam("page") int page, @RequestParam("size") int size,
                                             @RequestBody ApplicationInstance applicationInstance) {
        return this.applicationService.findApplicationInstance(PageRequest.of(page, size), applicationInstance);
    }
}
