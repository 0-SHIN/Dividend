package com.zerobase.persist;


import com.zerobase.persist.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {
    List<DividendEntity> findAllByCompanyId(Long id);

    boolean existsByCompanyIdAndDate(Long companyId, LocalDate date);

    @Transactional
    void deleteAllByCompanyId(Long id);
}
