package jbook.jshop.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * 로그 메시지에 'syiif'라는 문자열이 들어있으면 필터 통과
 */
public class SyiifKeywordFilter extends Filter<ILoggingEvent> {

    private static final String KEYWORD = "IF-777";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg.contains(KEYWORD)) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.NEUTRAL;
    }
}
