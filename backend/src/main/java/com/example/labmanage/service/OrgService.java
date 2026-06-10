package com.example.labmanage.service;

import com.example.labmanage.dto.OrgRequest;
import com.example.labmanage.dto.OrgResponse;
import com.example.labmanage.entity.OrgEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrgRepository orgRepository;

    public List<OrgResponse> list() {
        return orgRepository.findAll().stream().map(this::toResponse).toList();
    }

    public OrgResponse getById(long id) {
        return toResponse(orgRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("组织不存在: " + id)));
    }

    @Transactional
    public OrgResponse create(OrgRequest req) {
        if (orgRepository.findByCode(req.getCode()).isPresent()) {
            throw new IllegalArgumentException("组织编码已存在");
        }
        OrgEntity entity = new OrgEntity();
        copy(req, entity);
        return toResponse(orgRepository.save(Objects.requireNonNull(entity, "组织实体不能为空")));
    }

    @Transactional
    public OrgResponse update(long id, OrgRequest req) {
        OrgEntity entity = orgRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("组织不存在: " + id));
        if (orgRepository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new IllegalArgumentException("组织编码已存在");
        }
        if (req.getParentId() != null && req.getParentId().equals(id)) {
            throw new IllegalArgumentException("上级组织不能选择自己");
        }
        copy(req, entity);
        return toResponse(orgRepository.save(Objects.requireNonNull(entity, "组织实体不能为空")));
    }

    @Transactional
    public void delete(long id) {
        if (!orgRepository.existsById(id)) {
            throw new NotFoundException("组织不存在: " + id);
        }
        if (!orgRepository.findByParentId(id).isEmpty()) {
            throw new IllegalArgumentException("请先删除该组织的下级节点");
        }
        orgRepository.deleteById(id);
    }

    private void copy(OrgRequest req, OrgEntity entity) {
        entity.setCode(req.getCode().trim());
        entity.setName(req.getName().trim());
        entity.setLevel(req.getLevel());
        entity.setParentId(req.getParentId());
        entity.setLeader(req.getLeader());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : "启用");
    }

    private OrgResponse toResponse(OrgEntity entity) {
        OrgResponse resp = new OrgResponse();
        resp.setId(entity.getId());
        resp.setCode(entity.getCode());
        resp.setName(entity.getName());
        resp.setLevel(entity.getLevel());
        resp.setParentId(entity.getParentId());
        resp.setLeader(entity.getLeader());
        resp.setStatus(entity.getStatus());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }
}
