package kaboygarage.repository;

import kaboygarage.model.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository untuk SparePart.
 */
@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    
    @Query("FROM SparePart WHERE LOWER(kode) LIKE LOWER(:pola) OR LOWER(nama) LIKE LOWER(:pola) OR LOWER(merk) LIKE LOWER(:pola) ORDER BY kode")
    List<SparePart> findByKeyword(@Param("pola") String pola);
    
    @Query("FROM SparePart WHERE stok <= stokMinimal ORDER BY stok, kode")
    List<SparePart> findMenipis();
    
    @Query("SELECT COUNT(*) FROM SparePart WHERE stok <= stokMinimal")
    long countMenipis();
}
