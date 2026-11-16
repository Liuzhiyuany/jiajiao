package com.example.jiajiao.repository;

import com.example.jiajiao.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // 这是一个自定义查询方法：根据科目和年级找老师
    // Spring 看到这个方法名，会自动去数据库里生成对应的 SQL 语句
    List<Teacher> findBySubjectAndGrade(String subject, String grade);
}
