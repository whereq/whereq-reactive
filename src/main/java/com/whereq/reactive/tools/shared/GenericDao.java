package com.whereq.reactive.tools.shared;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GenericDao {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public Flux<ApplicationInstance> findAllApplicationInstance() {
        return this.r2dbcEntityTemplate.select(ApplicationInstance.class).all();
    }
    public Flux<ApplicationInstance> findApplicationInstance(PageRequest pageRequest, ApplicationInstance applicationInstance) {
        ReactiveSelectOperation.ReactiveSelect<ApplicationInstance> applicationInstanceReactiveSelect =
                this.r2dbcEntityTemplate.select(ApplicationInstance.class);

        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.isNotBlank(applicationInstance.getServer())) {
            criteriaList.add(Criteria.where("server").is(applicationInstance.getServer()));
        }
        if (StringUtils.isNotBlank(applicationInstance.getApp())) {
            criteriaList.add(Criteria.where("app").is(applicationInstance.getApp()));
        }
        if (StringUtils.isNotBlank(applicationInstance.getStatus())) {
            criteriaList.add(Criteria.where("status").is(applicationInstance.getStatus()));
        }
        return applicationInstanceReactiveSelect.matching(Query.query(Criteria.from(criteriaList))).all();
    }

}


