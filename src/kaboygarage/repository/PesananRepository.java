package kaboygarage.repository;

import kaboygarage.model.Pesanan;
import kaboygarage.model.StatusPesanan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository untuk Pesanan.
 */
@Repository
public interface PesananRepository extends JpaRepository<Pesanan, Long> {
    
    @Query("SELECT COUNT(*) FROM Pesanan WHERE status NOT IN ('SELESAI', 'DIBATALKAN')")
    long countAktif();
}
