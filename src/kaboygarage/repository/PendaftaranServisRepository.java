package kaboygarage.repository;

import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.StatusServis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository untuk PendaftaranServis.
 */
@Repository
public interface PendaftaranServisRepository extends JpaRepository<PendaftaranServis, Long> {
    
    @Query("SELECT COUNT(*) FROM PendaftaranServis WHERE status NOT IN ('SELESAI', 'DIBATALKAN')")
    long countAktif();
}
