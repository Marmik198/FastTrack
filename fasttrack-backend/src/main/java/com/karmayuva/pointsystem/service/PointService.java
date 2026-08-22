package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.ChildPointsSummaryDto;
import com.karmayuva.pointsystem.dto.PointDto;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.entity.Point;
import com.karmayuva.pointsystem.exception.BusinessException;
import com.karmayuva.pointsystem.repository.ChildRepository;
import com.karmayuva.pointsystem.repository.PointRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final PointRepository pointRepository;
    private final ChildRepository childRepository;

    public PointService(PointRepository pointRepository, ChildRepository childRepository) {
        this.pointRepository = pointRepository;
        this.childRepository = childRepository;
    }

    public Page<PointDto> search(Long childId, String month, Pageable pageable) {
        return pointRepository.search(childId, month, pageable).map(this::toDto);
    }

    public int getAvailableBalance(Long childId) {
        int accrued = pointRepository.sumValueByChildId(childId);
        int burnt = pointRepository.sumBurntByChildId(childId);
        return accrued - burnt;
    }

    // Merged view: every child alongside their accrued/burnt/available totals
    public Page<ChildPointsSummaryDto> pointsSummary(String search, Pageable pageable) {
        Page<Child> children = childRepository.search(search, pageable);
        return children.map(child -> {
            int accrued = pointRepository.sumValueByChildId(child.getId());
            int burnt = pointRepository.sumBurntByChildId(child.getId());
            return new ChildPointsSummaryDto(
                    child.getId(),
                    child.getFirstName(),
                    child.getLastName(),
                    child.getMobile(),
                    child.getEmail(),
                    accrued,
                    burnt,
                    accrued - burnt
            );
        });
    }

    /**
     * Burns `amount` points off a child's balance, FIFO across their Point rows
     * (oldest accrual first). Throws if the child doesn't have enough available.
     */
    public void burnPoints(Long childId, int amount) {
        if (amount <= 0) {
            throw new BusinessException("Amount to burn must be positive");
        }

        List<Point> rows = pointRepository.findByChildIdOrderByCreationDateAsc(childId);
        int available = rows.stream().mapToInt(p -> p.getValue() - p.getBurnt()).sum();

        if (available < amount) {
            throw new BusinessException("Insufficient points: available " + available + ", requested " + amount);
        }

        int remaining = amount;
        for (Point row : rows) {
            if (remaining <= 0) break;
            int rowAvailable = row.getValue() - row.getBurnt();
            if (rowAvailable <= 0) continue;

            int toBurnFromRow = Math.min(rowAvailable, remaining);
            row.setBurnt(row.getBurnt() + toBurnFromRow);
            remaining -= toBurnFromRow;
            pointRepository.save(row);
        }
    }

    private PointDto toDto(Point p) {
        return new PointDto(
                p.getId(),
                p.getMonth(),
                p.getValue(),
                p.getBurnt(),
                p.getValue() - p.getBurnt(),
                p.getChildId(),
                p.getChildName(),
                p.getAccrualId(),
                p.getCreationDate() != null ? p.getCreationDate().toString() : null,
                p.getLastModifiedDate() != null ? p.getLastModifiedDate().toString() : null
        );
    }
}
