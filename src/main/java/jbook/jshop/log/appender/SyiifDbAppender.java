package jbook.jshop.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jbook.jshop.dto.SyiifLogDto;
import jbook.jshop.mapper.SyiifLogMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SyiifDbAppender extends AppenderBase<ILoggingEvent> {

    @Autowired
    private SyiifLogMapper syiifLogMapper;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void append(ILoggingEvent event) {
        // Filter에서 syiif 키워드 체크 -> 여기 들어옴
        String message = event.getFormattedMessage();

        try {
            // 메시지가 syiif + JSON이라고 가정 -> 파싱
            JsonNode root = objectMapper.readTree(message);

            String interfaceId = getSafe(root, "interfaceId");
            String headerKey   = getSafe(root, "headerKey");
            String name        = getSafe(root, "name");
            String msgValue    = getSafe(root, "message");

            // 방어 로직: 4개 필드가 전부 없어도 DB에는 rawData만 넣고 싶을 수도 있음
            // 여기선 최소 한 개라도 있으면 Insert한다고 가정
            if (interfaceId.isEmpty() && headerKey.isEmpty() && name.isEmpty() && msgValue.isEmpty()) {
                addInfo("[SyiifDbAppender] All 4 fields missing, skip insert. msg=" + message);
                return;
            }

            // DTO 생성 및 필드 설정
            SyiifLogDto dto = new SyiifLogDto();
            dto.setInterfaceId(interfaceId);
            dto.setHeaderKey(headerKey);
            dto.setName(name);
            dto.setMessage(msgValue);
            dto.setRawData(message);

            // Mapper Insert
            if (syiifLogMapper != null) {
                syiifLogMapper.insertIfLog(dto);
            } else {
                addWarn("[SyiifDbAppender] syiifLogMapper is null, cannot insert DB");
            }

        } catch (Exception e) {
            // JSON 파싱 실패 or 기타
            addError("[SyiifDbAppender] Failed to parse/insert syiif JSON", e);
        }
    }

    private String getSafe(JsonNode node, String key) {
        if (!node.has(key)) return "";
        return node.get(key).asText("");
    }
}