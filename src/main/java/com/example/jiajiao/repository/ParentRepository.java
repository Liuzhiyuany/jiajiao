package com.example.jiajiao.repository;

import com.example.jiajiao.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    // 老师找学生用：根据需求科目和年级找家长
    List<Parent> findByDemandSubjectAndDemandGrade(String demandSubject, String demandGrade);
}