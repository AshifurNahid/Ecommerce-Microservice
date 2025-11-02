package com.nahid.audit.service;

import com.nahid.audit.dto.AuditEventDTO;
import com.nahid.audit.mapper.AuditMapper;
import com.nahid.audit.repository.AuditRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;

    public AuditService(AuditRepository auditRepository, AuditMapper auditMapper) {
        this.auditRepository = auditRepository;
        this.auditMapper = auditMapper;
    }

    public void processAuditEvent(AuditEventDTO auditEventDTO){
        auditRepository.save(auditMapper.toEntity(auditEventDTO));
    }
}
