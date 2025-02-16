package jbook.jshop.config;

import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import jbook.jshop.log.appender.SyiifDbAppender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogbackConfig {

    @Autowired
    private SyiifDbAppender syiifDbAppender;

    @PostConstruct
    public void init() {
        // 1) Logback의 LoggerContext 획득
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // 2) 필요한 필터도 syiifDbAppender에 등록 (syiif 키워드 필터 등)
        //    syiifDbAppender.addFilter(new SyiifKeywordFilter());
        syiifDbAppender.setContext(context);
        syiifDbAppender.start();

        // 3) 루트 로거에 이 Appender를 붙인다
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(syiifDbAppender);
    }
}

