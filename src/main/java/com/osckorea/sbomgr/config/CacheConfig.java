package com.osckorea.sbomgr.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.osckorea.sbomgr.service.SbomService;
import com.osckorea.sbomgr.util.json.SbomJsonParser;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public LoadingCache<String, SbomJsonParser.CpeParts> cpeCache(@Lazy SbomJsonParser sbomJsonParser) { // @Lazy 추가
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(sbomJsonParser::parseCpe);
    }

}
