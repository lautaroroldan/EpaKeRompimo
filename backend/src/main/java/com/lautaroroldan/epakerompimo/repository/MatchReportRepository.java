package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.MatchReport;
import com.lautaroroldan.epakerompimo.enums.MatchReportStatus;
import com.lautaroroldan.epakerompimo.enums.MatchReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchReportRepository extends JpaRepository<MatchReport, Long> {

    List<MatchReport> findAllByOrderByCreatedAtDesc();

    List<MatchReport> findByStatusOrderByCreatedAtDesc(MatchReportStatus status);

    List<MatchReport> findByTypeOrderByCreatedAtDesc(MatchReportType type);

    List<MatchReport> findByStatusAndTypeOrderByCreatedAtDesc(MatchReportStatus status, MatchReportType type);

    List<MatchReport> findByUser_IdUserOrderByCreatedAtDesc(Long idUser);

    // Se usa al eliminar una cuenta
    void deleteByUser_IdUser(Long idUser);
}
