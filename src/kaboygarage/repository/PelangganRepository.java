package kaboygarage.repository;

import kaboygarage.model.Pelanggan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository untuk Pelanggan.
 */
@Repository
public interface PelangganRepository extends JpaRepository<Pelanggan, Long> {
}
