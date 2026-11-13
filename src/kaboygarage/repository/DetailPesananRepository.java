package kaboygarage.repository;

import kaboygarage.model.DetailPesanan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository untuk DetailPesanan.
 */
@Repository
public interface DetailPesananRepository extends JpaRepository<DetailPesanan, Long> {
}
