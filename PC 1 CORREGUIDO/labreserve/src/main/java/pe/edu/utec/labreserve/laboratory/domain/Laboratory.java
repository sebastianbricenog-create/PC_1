package pe.edu.utec.labreserve.laboratory.domain;

import jakarta.persistence.*;
import lombok.*;
import pe.edu.utec.labreserve.user.domain.User;

@Entity
@Table(name = "laboratories",
        uniqueConstraints = @UniqueConstraint(name = "uk_laboratories_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Laboratory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LaboratoryStatus status;
}
