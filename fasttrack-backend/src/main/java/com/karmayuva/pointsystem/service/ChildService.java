package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.ChildDto;
import com.karmayuva.pointsystem.dto.ChildUpsertRequest;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.exception.ResourceNotFoundException;
import com.karmayuva.pointsystem.repository.ChildRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
