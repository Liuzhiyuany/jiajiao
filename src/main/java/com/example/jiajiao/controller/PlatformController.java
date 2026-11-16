package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Parent;
import com.example.jiajiao.entity.Teacher;
import com.example.jiajiao.repository.ParentRepository;
import com.example.jiajiao.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PlatformController {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ParentRepository parentRepository; // 注入家长的数据仓库

    // 真实的 POST 注册接口
    @PostMapping("/teacher")
    public Teacher saveTeacher(@RequestBody Teacher teacher) {
        return teacherRepository.save(teacher);
    }


    /// @param subject
    /// @param grade
    /// @return
    // 智能查询：传了什么查什么，全都不传就查所有
    @GetMapping("/teachers")
    public List<Teacher> searchTeachers(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {

        // 先把数据库里的所有老师都拿出来
        List<Teacher> allTeachers = teacherRepository.findAll();

        // 用 Java 的 Stream 流进行过滤
        return allTeachers.stream().filter(t -> {
            // 如果前端没传科目，或者老师科目包含前端传的字，就符合条件
            boolean matchSubject = (subject == null || subject.isEmpty()) || t.getSubject().contains(subject);
            // 如果前端没传年级，或者老师年级包含前端传的字，就符合条件
            boolean matchGrade = (grade == null || grade.isEmpty()) || t.getGrade().contains(grade);

            // 只有科目和年级都符合，这个老师才会被返回
            return matchSubject && matchGrade;
        }).toList();
    }
    // 家长发布需求接口
    @PostMapping("/parent")
    public Parent saveParent(@RequestBody Parent parent) {
        return parentRepository.save(parent);
    }

    // 老师搜索家长需求
    @GetMapping("/parents")
    public List<Parent> searchParents(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {
        List<Parent> allParents = parentRepository.findAll();
        return allParents.stream().filter(p -> {
            boolean matchSubject = (subject == null || subject.isEmpty()) || p.getDemandSubject().contains(subject);
            boolean matchGrade = (grade == null || grade.isEmpty()) || p.getDemandGrade().contains(grade);
            return matchSubject && matchGrade;
        }).toList();
    }
}