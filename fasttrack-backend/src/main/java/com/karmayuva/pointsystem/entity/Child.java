package com.karmayuva.pointsystem.entity;

import com.karmayuva.pointsystem.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Master data for each child, maintained by the admin team and updated as needed.
 *
 * ASSUMPTION: an `address` field was added even though it wasn't in the originally
 * listed columns, because the public form is required to display the child's address
 * alongside email/mobile/name once they're matched. Remove it if that wasn't intended.
 *
 * PII fields (mobile, alternateMobile, email, address) are encrypted at rest.
 * firstName/lastName/motherName/fatherName are left unencrypted since fuzzy name
 * matching and admin search both need to work against them.
 */
@Entity
@Table(name = "children")
@Getter
@Setter
@NoArgsConstructor
public class Child extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String motherName;

    private String fatherName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mobile;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String alternateMobile;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String email;

    private Integer age;

    private LocalDate dateOfBirth;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String address;

    @Transient
    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
