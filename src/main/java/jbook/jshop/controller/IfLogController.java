package jbook.jshop.controller;


import jbook.jshop.dto.SyiifLogDto;
import jbook.jshop.mapper.SyiifLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class IfLogController {

    private final SyiifLogMapper syiifLogMapper;

    @GetMapping("/api/if-logs")
    public List<SyiifLogDto> getIfLogs() {
        List<SyiifLogDto> syiifLogDtos = syiifLogMapper.selectIfLogs();
        return syiifLogDtos;
    }
}
