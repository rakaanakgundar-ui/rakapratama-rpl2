package kaboygarage.repository;

import kaboygarage.model.Kendaraan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository untuk Kendaraan.
 */
@Repository
public interface KendaraanRepository extends JpaRepository<Kendaraan, Long> {
    List<Kendaraan> findByPelangganId(Long pelangganId);
}
