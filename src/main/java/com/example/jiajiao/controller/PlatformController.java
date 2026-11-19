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

    // 家长确认结束需求（只有发布者本人能操作）
    @PutMapping("/parents/{id}/finish")
    public java.util.Map<String, Object> finishParentDemand(
            @PathVariable Long id,
            @RequestParam Long userId) {

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        Parent parent = parentRepository.findById(id).orElse(null);

        if (parent == null) {
            result.put("success", false);
            result.put("message", "需求不存在");
            return result;
        }
        // 只有发布这条需求的家长本人，才能让它下架
        if (parent.getUserId() == null || !parent.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "只有发布这条需求的家长才能结束它");
            return result;
        }
        if (parent.getStatus() != null && parent.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "这条需求已经结束了");
            return result;
        }

        parent.setStatus(0);
        parentRepository.save(parent);

        result.put("success", true);
        result.put("message", "需求已结束，不再出现在大厅");
        return result;
    }
    // 家长查看自己发布的所有需求（含已结束的），并附带"有几位老师联系过我"
    @GetMapping("/my-demands")
    public List<java.util.Map<String, Object>> myDemands(
            @RequestParam Long userId){

        return parentRepository.findAll().stream()
                .filter(p -> p.getUserId() != null && p.getUserId().equals(userId))
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))   // 新的在前
                .map(p -> {
                    // 统计有多少个不同账号给我发过私信（= 有多少位老师联系过我）
                    long applyCount = messageRepository.findAll().stream()
                            .filter(m -> p.getUserId().equals(m.getToUserId()))
                            .map(m -> m.getFromUserId())
                            .distinct()
                            .count();

                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("demand", p);
                    map.put("applyCount", applyCount);
                    return map;
                })
                .toList();
    }
    // 根据账号ID（userId）查询用户基础信息接口
    @GetMapping("/users/{id}")
    public com.example.jiajiao.entity.User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }
    @Autowired
    private com.example.jiajiao.repository.MessageRepository messageRepository;
    // 老师"申请接单"：只给家长发一条私信，需求仍留在大厅，等家长确认
    @PostMapping("/apply")
    public java.util.Map<String, Object> applyDemand(
            @RequestParam Long teacherUserId,
            @RequestParam Long parentDemandId,
            @RequestParam(required = false) String text) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        Parent parent = parentRepository.findById(parentDemandId).orElse(null);

        if (parent == null) {
            result.put("success", false);
            result.put("message", "需求不存在");
            return result;
        }
        if (parent.getStatus() != null && parent.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "这个需求已经结束了");
            return result;
        }
        if (parent.getUserId() == null) {
            result.put("success", false);
            result.put("message", "需求数据异常，无法申请");
            return result;
        }
        if (parent.getUserId().equals(teacherUserId)) {
            result.put("success", false);
            result.put("message", "不能申请自己发布的需求");
            return result;
        }

        // 不改状态，只落一条私信记录，让家长在消息中心看到
        com.example.jiajiao.entity.Message m = new com.example.jiajiao.entity.Message();
        m.setFromUserId(teacherUserId);
        m.setFromRole("TEACHER");
        m.setToUserId(parent.getUserId());
        m.setToRole("PARENT");
        m.setContent((text == null || text.isBlank())
                ? "您好，我想接这个家教需求，方便沟通一下吗？"
                : text);
        m.setSendTime(java.time.LocalDateTime.now());
        messageRepository.save(m);

        result.put("success", true);
        result.put("message", "申请已发送，等待家长确认");
        return result;
    }
    // 查询某个账号对应的老师资料（用于申请接单时自动生成自我介绍）
    @GetMapping("/teacher/my")
    public Teacher myTeacher(@RequestParam Long userId) {
        return teacherRepository.findAll().stream()
                .filter(t -> t.getUserId() != null && t.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}
