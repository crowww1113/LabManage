package com.example.labmanage.service;

import com.example.labmanage.dto.ScheduleNoticeDTO;
import com.example.labmanage.entity.ScheduleNoticeEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.ScheduleNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleNoticeService {
    private final ScheduleNoticeRepository repository;

    public List<ScheduleNoticeDTO> list(Long receiverId, String readStatus) {
        List<ScheduleNoticeEntity> list;
        if (receiverId != null && readStatus != null && !readStatus.isBlank()) {
            list = repository.findByReceiverIdAndReadStatusAndDeletedFalse(receiverId, readStatus);
        } else if (receiverId != null) {
            list = repository.findByReceiverIdAndDeletedFalse(receiverId);
        } else {
            list = repository.findAll().stream().filter(entity -> !Boolean.TRUE.equals(entity.getDeleted())).toList();
        }
        return list.stream().map(this::toDTO).toList();
    }

    public ScheduleNoticeDTO getById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public ScheduleNoticeDTO markAsRead(Long id) {
        ScheduleNoticeEntity entity = getEntity(id);
        entity.setReadStatus("READ");
        entity.setReadTime(LocalDateTime.now());
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void createNotice(String bizType, Long bizId, Long senderId, Long receiverId,
                             String title, String content, String noticeType) {
        if (receiverId == null) {
            return;
        }
        ScheduleNoticeEntity entity = new ScheduleNoticeEntity();
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        entity.setSenderId(senderId == null ? 0L : senderId);
        entity.setReceiverId(receiverId);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setNoticeType(noticeType);
        entity.setReadStatus("UNREAD");
        entity.setDeleted(false);
        repository.save(entity);
    }

    private ScheduleNoticeEntity getEntity(Long id) {
        ScheduleNoticeEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("站内通知不存在"));
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new NotFoundException("站内通知不存在");
        }
        return entity;
    }

    private ScheduleNoticeDTO toDTO(ScheduleNoticeEntity entity) {
        ScheduleNoticeDTO dto = new ScheduleNoticeDTO();
        dto.setId(entity.getId());
        dto.setBizType(entity.getBizType());
        dto.setBizId(entity.getBizId());
        dto.setSenderId(entity.getSenderId());
        dto.setReceiverId(entity.getReceiverId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setNoticeType(entity.getNoticeType());
        dto.setReadStatus(entity.getReadStatus());
        dto.setSendTime(entity.getSendTime());
        dto.setReadTime(entity.getReadTime());
        return dto;
    }
}
