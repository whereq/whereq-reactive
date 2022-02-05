package com.whereq.reactive.tools.shared;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ApplicationRepository extends ReactiveCrudRepository<Application, Long> {
    
}
