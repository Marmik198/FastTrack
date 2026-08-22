package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.AccrualCreateRequest;
import com.karmayuva.pointsystem.dto.AccrualDto;
import com.karmayuva.pointsystem.entity.Accrual;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.entity.Point;
import com.karmayuva.pointsystem.repository.AccrualRepository;
import com.karmayuva.pointsystem.repository.PointRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AccrualService {

    private static final int POINTS_PER_FAST = 100;

    private final AccrualRepository accrualRepository;
    private final PointRepository pointRepository;
    private final ChildService childService;

    public AccrualService(AccrualRepository accrualRepository, PointRepository pointRepository, ChildService childService) {
        this.accrualRepository = accrualRepository;
        this.pointRepository = pointRepository;
        this.childService = childService;
    }

    public Page<AccrualDto> search(Long childId, String month, Pageable pageable) {
        return accrualRepository.search(childId, month, pageable).map(this::toDto);
    }

    @Transactional
    public AccrualDto create(AccrualCreateRequest request) {
        Child child = childService.getEntity(request.childId());
        Accrual accrual = createAccrualAndPoint(child, request.month(), request.frequency());
        return toDto(accrual);
    }

    /**
     * Creates the Accrual row plus its corresponding Point row (value = frequency * 100),
     * as specified. Used both by the manual single-entry endpoint and the bulk importer.
     */
    Accrual createAccrualAndPoint(Child child, String month, int frequency) {
        Accrual accrual = new Accrual();
        accrual.setChildId(child.getId());
        accrual.setChildName(child.getFullName());
        accrual.setMonth(month);
        accrual.setFrequency(frequency);
        accrual = accrualRepository.save(accrual);

        Point point = new Point();
        point.setChildId(child.getId());
        point.setChildName(child.getFullName());
        point.setMonth(month);
        point.setValue(frequency * POINTS_PER_FAST);
        point.setBurnt(0);
        point.setAccrualId(accrual.getId());
        pointRepository.save(point);

        return accrual;
    }

    private AccrualDto toDto(Accrual a) {
        return new AccrualDto(
                a.getId(),
                a.getMonth(),
                a.getFrequency(),
                a.getChildId(),
                a.getChildName(),
                a.getCreationDate() != null ? a.getCreationDate().toString() : null,
                a.getLastModifiedDate() != null ? a.getLastModifiedDate().toString() : null
        );
    }
}
