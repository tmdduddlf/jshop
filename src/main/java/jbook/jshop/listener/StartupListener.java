package jbook.jshop.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class StartupListener {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("■■■■■■■■■■■■■■■■■■■■■■■■■■■");
        log.info("서버 구동이 완료되었습니다.");
        log.info("■■■■■■■■■■■■■■■■■■■■■■■■■■■");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void copyDirectoryAfterStartup() {
        File source = new File("C:\\Users\\ambig\\Downloads\\abc");
        File destination = new File("C:\\destination\\abc");

        try {
            // 1) source 폴더가 없으면 생성
            if (!source.exists()) {
                log.info("[폴더복사] Source 폴더가 없어서 새로 생성합니다: {}", source.getAbsolutePath());
                source.mkdirs();
            }

            // 2) destination 폴더가 없으면 생성
            if (!destination.exists()) {
                log.info("[폴더복사] Destination 폴더가 없어서 새로 생성합니다: {}", destination.getAbsolutePath());
                destination.mkdirs();
            }

            // 3) 폴더 전체 복사 (하위 파일, 디렉토리 포함)
            FileUtils.copyDirectory(source, destination);
            log.info("=== Folder copy completed from {} to {} ===", source, destination);

        } catch (IOException e) {
            log.error("Failed to copy folder: {}", e.getMessage(), e);
            throw new RuntimeException("폴더 복사 실패", e);
        }
    }
}
