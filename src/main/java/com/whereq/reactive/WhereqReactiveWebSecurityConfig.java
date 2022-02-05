package com.whereq.reactive;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@EnableWebFluxSecurity
public class WhereqReactiveWebSecurityConfig {
	
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http
				.securityMatcher(new NegatedServerWebExchangeMatcher(
						ServerWebExchangeMatchers.pathMatchers("/**")))
				.authorizeExchange().anyExchange().authenticated().and().httpBasic().and().csrf().disable().build();
	}

}


  
  
 
