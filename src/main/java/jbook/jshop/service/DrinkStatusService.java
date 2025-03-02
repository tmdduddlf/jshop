// src/main/java/com/example/service/DrinkStatusService.java
package jbook.jshop.service;

import jbook.jshop.dto.DrinkStatus;
import jbook.jshop.mapper.DrinkStatusMapper;
import jbook.jshop.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class DrinkStatusService {

    @Autowired
    private DrinkStatusMapper mapper;

    public List<DrinkStatus> getStatuses(String startDate, String endDate) {
        return mapper.findByDateRange(startDate, endDate);
    }

    public DrinkStatus getStatus(Long id) {
        return mapper.findById(id);
    }

    public void addStatus(DrinkStatus status) {
        String now = DateUtil.format(new Date());
        status.setRegDate(now);
        status.setUpdDate(now);
        mapper.insert(status);
    }

    public void updateStatus(DrinkStatus status) {
        status.setUpdDate(DateUtil.format(new Date()));
        mapper.update(status);
    }

    public void deleteStatus(Long id) {
        mapper.delete(id);
    }
}
