package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.ChildDto;
import com.karmayuva.pointsystem.dto.ChildImportResultDto;
import com.karmayuva.pointsystem.dto.ChildUpsertRequest;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.exception.ResourceNotFoundException;
import com.karmayuva.pointsystem.repository.ChildRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChildService {

    private final ChildRepository childRepository;

    public ChildService(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    public Page<ChildDto> search(String search, Pageable pageable) {
        return childRepository.search(search, pageable).map(this::toDto);
    }

    public ChildDto getById(Long id) {
        return toDto(getEntity(id));
    }

    public Child getEntity(Long id) {
        return childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));
    }

    public ChildDto create(ChildUpsertRequest request) {
        Child child = new Child();
        applyRequest(child, request);
        return toDto(childRepository.save(child));
    }

    public ChildDto update(Long id, ChildUpsertRequest request) {
        Child child = getEntity(id);
        applyRequest(child, request);
        return toDto(childRepository.save(child));
    }

    public void delete(Long id) {
        Child child = getEntity(id);
        childRepository.delete(child);
    }

    private void applyRequest(Child child, ChildUpsertRequest request) {
        child.setFirstName(request.firstName().trim());
        child.setLastName(request.lastName().trim());
        child.setMotherName(request.motherName());
        child.setFatherName(request.fatherName());
        child.setMobile(request.mobile().trim());
        child.setAlternateMobile(request.alternateMobile());
        child.setEmail(request.email());
        child.setAge(request.age());
        child.setDateOfBirth(request.dateOfBirth() != null && !request.dateOfBirth().isBlank()
                ? LocalDate.parse(request.dateOfBirth()) : null);
        child.setAddress(request.address());
    }

    public ChildImportResultDto createMany(List<ChildUpsertRequest> requests) {
        List<String> errors = new ArrayList<>();
        int created = 0;
        int skipped = 0;

        // Mobile is encrypted at rest, so a SQL "already exists" check isn't possible -
        // duplicates are detected in memory instead, same approach as NameMatchingService.
        Set<String> existingMobiles = childRepository.findAll().stream()
                .map(Child::getMobile)
                .filter(m -> m != null && !m.isBlank())
                .collect(Collectors.toCollection(HashSet::new));

        for (int i = 0; i < requests.size(); i++) {
            int recordNum = i + 1;
            ChildUpsertRequest request = requests.get(i);
            try {
                if (isBlank(request.firstName()) || isBlank(request.lastName()) || isBlank(request.mobile())) {
                    errors.add("Record " + recordNum + ": firstName, lastName and mobile are required");
                    continue;
                }
                String mobile = request.mobile().trim();
                if (!mobile.matches("^[6-9]\\d{9}$")) {
                    errors.add("Record " + recordNum + ": invalid mobile number '" + mobile + "'");
                    continue;
                }
                if (existingMobiles.contains(mobile)) {
                    errors.add("Record " + recordNum + ": skipped - a child with mobile " + mobile + " already exists");
                    skipped++;
                    continue;
                }
                create(request);
                existingMobiles.add(mobile);
                created++;
            } catch (Exception e) {
                errors.add("Record " + recordNum + ": " + e.getMessage());
            }
        }

        return new ChildImportResultDto(requests.size(), created, skipped, errors);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ChildDto toDto(Child c) {
        return new ChildDto(
                c.getId(),
                c.getFirstName(),
                c.getLastName(),
                c.getMotherName(),
                c.getFatherName(),
                c.getMobile(),
                c.getAlternateMobile(),
                c.getEmail(),
                c.getAge(),
                c.getDateOfBirth() != null ? c.getDateOfBirth().toString() : null,
                c.getAddress(),
                c.getCreationDate() != null ? c.getCreationDate().toString() : null,
                c.getLastModifiedDate() != null ? c.getLastModifiedDate().toString() : null
        );
    }
}
