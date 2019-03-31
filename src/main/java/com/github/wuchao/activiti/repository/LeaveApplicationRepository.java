package com.github.wuchao.activiti.repository;

import com.github.wuchao.activiti.domain.LeaveApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findAllByApplicantIdAndDeletedIsFalseOrderByIdDesc(Long applicantId);

    @EntityGraph(attributePaths = "applicant")
    Optional<LeaveApplication> findTopById(Long applicationId);

}
