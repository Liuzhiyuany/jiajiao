package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Parent;
import com.example.jiajiao.entity.Teacher;
import com.example.jiajiao.repository.ParentRepository;
import com.example.jiajiao.repository.TeacherRepository;
import com.example.jiajiao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PlatformController {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private UserRepository userRepository;

    // 保存教师资料，接收前端传来的userId存入teacher表
    @PostMapping("/teacher")
    public Teacher saveTeacher(@RequestBody Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    // 老师查询接口（新增初中/高中聚合搜索）
    @GetMapping("/teachers")
    public List<Teacher> searchTeachers(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {

        List<Teacher> allTeachers = teacherRepository.findAll();
        return allTeachers.stream().filter(t -> {
            boolean matchSubject = (subject == null || subject.isEmpty()) || t.getSubject().contains(subject);
            boolean matchGrade = matchGrade(grade, t.getGrade());
            return matchSubject && matchGrade;
        }).toList();
    }

    // 家长发布需求接口，接收前端传来的userId存入parent表
    @PostMapping("/parent")
    public Parent saveParent(@RequestBody Parent parent) {
        return parentRepository.save(parent);
    }

    // 老师搜索家长需求（新增初中/高中聚合搜索）
    @GetMapping("/parents")
    public List<Parent> searchParents(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {
        List<Parent> allParents = parentRepository.findAll();
        return allParents.stream().filter(p -> {
            if (p.getStatus() != null && p.getStatus() == 0) {
                return false;
            }
            boolean matchSubject = (subject == null || subject.isEmpty()) || p.getDemandSubject().contains(subject);
            boolean matchGrade = matchGrade(grade, p.getDemandGrade());
            return matchSubject && matchGrade;
        }).toList();
    }
    /**
     * 年级匹配工具方法
     * 1. 输入「初中」→ 匹配初一、初二、初三
     * 2. 输入「高中」→ 匹配高一、高二、高三
     * 3. 其他关键词 → 保留原有模糊包含匹配（搜「初三」「初」都生效）
     * 4. 为空则不筛选
     */
    private boolean matchGrade(String searchKeyword, String actualGrade) {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            return true;
        }
        String keyword = searchKeyword.trim();
        if ("初中".equals(keyword)) {
            return "初一".equals(actualGrade)
                    || "初二".equals(actualGrade)
                    || "初三".equals(actualGrade);
        } else if ("高中".equals(keyword)) {
            return "高一".equals(actualGrade)
                    || "高二".equals(actualGrade)
                    || "高三".equals(actualGrade);
        } else {
            // 保留你原来的模糊匹配逻辑
            return actualGrade != null && actualGrade.contains(keyword);
        }
    }

    // 接单/关闭需求接口
    @PutMapping("/parents/{id}/finish")
    public void finishParentDemand(@PathVariable Long id) {
        Parent parent = parentRepository.findById(id).orElse(null);
        if (parent != null) {
            parent.setStatus(0);
            parentRepository.save(parent);
        }
    }

    // 根据账号ID（userId）查询用户基础信息接口
    @GetMapping("/users/{id}")
    public com.example.jiajiao.entity.User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
