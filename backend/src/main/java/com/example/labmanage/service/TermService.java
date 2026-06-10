package com.example.labmanage.service;

import com.example.labmanage.dto.TermCalendarDTO;
import com.example.labmanage.dto.TermDTO;
import com.example.labmanage.entity.TermEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.TermRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TermService {
    private final TermRepository termRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TermDTO> list() {
        return termRepository.findAll().stream().map(this::toDTO).toList();
    }

    public TermDTO getById(Long id) {
        return toDTO(termRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("学期不存在")));
    }

    @Transactional
    public TermDTO create(TermDTO dto) {
        TermEntity entity = new TermEntity();
        copy(dto, entity);
        // 自动计算总周数
        long days = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate());
        entity.setTotalWeeks((int) Math.ceil(days / 7.0));
        return toDTO(termRepository.save(entity));
    }

    @Transactional
    public TermDTO update(Long id, TermDTO dto) {
        TermEntity entity = termRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("学期不存在"));
        copy(dto, entity);
        long days = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate());
        entity.setTotalWeeks((int) Math.ceil(days / 7.0));
        return toDTO(termRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!termRepository.existsById(id)) throw new NotFoundException("学期不存在");
        termRepository.deleteById(id);
    }

    // 生成完整学期日历
    public List<TermCalendarDTO> getCalendar(Long termId) {
        TermEntity term = termRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("学期不存在"));
        List<String> holidays = parseHolidays(term.getHolidayDates());
        List<String> labOpenDays = parseLabOpenDays(term.getLabOpenDays());
        List<TermCalendarDTO> calendar = new ArrayList<>();

        LocalDate current = term.getStartDate();
        while (!current.isAfter(term.getEndDate())) {
            TermCalendarDTO dto = new TermCalendarDTO();
            dto.setDate(current);
            // 计算周次
            long daysDiff = ChronoUnit.DAYS.between(term.getStartDate(), current);
            dto.setWeek((int) (daysDiff / 7 + 1));
            dto.setDayOfWeek(current.getDayOfWeek().getValue());
            // 判断是否节假日
            dto.setIsHoliday(holidays.contains(current.toString()));
            dto.setHolidayName(dto.getIsHoliday() ? "节假日" : "");
            // 判断是否实验室开放日
            dto.setIsLabOpen(labOpenDays.contains(current.toString()));
            calendar.add(dto);
            current = current.plusDays(1);
        }
        return calendar;
    }

    // 根据日期查询周次
    public Integer getWeekByDate(Long termId, LocalDate date) {
        TermEntity term = termRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("学期不存在"));
        if (date.isBefore(term.getStartDate()) || date.isAfter(term.getEndDate())) {
            throw new NotFoundException("日期不在学期范围内");
        }
        long daysDiff = ChronoUnit.DAYS.between(term.getStartDate(), date);
        return (int) (daysDiff / 7 + 1);
    }

    // 根据周次查询日期范围
    public List<LocalDate> getDateByWeek(Long termId, Integer week) {
        TermEntity term = termRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("学期不存在"));
        LocalDate start = term.getStartDate().plusWeeks(week - 1);
        LocalDate end = start.plusDays(6);
        if (end.isAfter(term.getEndDate())) end = term.getEndDate();
        return Arrays.asList(start, end);
    }

    private List<String> parseHolidays(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<String> parseLabOpenDays(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void copy(TermDTO dto, TermEntity entity) {
        entity.setTermName(dto.getTermName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        try {
            entity.setHolidayDates(objectMapper.writeValueAsString(dto.getHolidayDates()));
        } catch (Exception e) {
            entity.setHolidayDates("[]");
        }
        try {
            entity.setLabOpenDays(objectMapper.writeValueAsString(dto.getLabOpenDays()));
        } catch (Exception e) {
            entity.setLabOpenDays("[]");
        }
    }

    private TermDTO toDTO(TermEntity entity) {
        TermDTO dto = new TermDTO();
        dto.setId(entity.getId());
        dto.setTermName(entity.getTermName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setTotalWeeks(entity.getTotalWeeks());
        dto.setHolidayDates(parseHolidays(entity.getHolidayDates()));
        dto.setLabOpenDays(parseLabOpenDays(entity.getLabOpenDays()));

        // 根据当前日期计算 status
        LocalDate now = LocalDate.now();
        if (now.isBefore(entity.getStartDate())) {
            dto.setStatus("0"); // 未开始
        } else if (now.isAfter(entity.getEndDate())) {
            dto.setStatus("-1"); // 已结束
        } else {
            dto.setStatus("1"); // 进行中
        }

        return dto;
    }
}