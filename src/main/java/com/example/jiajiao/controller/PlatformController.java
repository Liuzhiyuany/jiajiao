package com.example.jiajiao.controller;

import com.example.jiajiao.entity.Message;
import com.example.jiajiao.entity.Parent;
import com.example.jiajiao.entity.Teacher;
import com.example.jiajiao.entity.User;
import com.example.jiajiao.repository.MessageRepository;
import com.example.jiajiao.repository.ParentRepository;
import com.example.jiajiao.repository.TeacherRepository;
import com.example.jiajiao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlatformController {
    @Autowired
    private com.example.jiajiao.security.CurrentUser currentUser;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    // ================== 老师资料 ==================

    // 保存老师资料（一个账号只保留一份，有就更新）
    @PostMapping("/teacher")
    public Teacher saveTeacher(@RequestBody Teacher teacher) {
        Long uid = currentUser.id();
        if (uid != null) teacher.setUserId(uid);          // 有 token 就强制用 token 的身份
        if (teacher.getUserId() == null) throw new IllegalArgumentException("请先登录");
        if (isBlank(teacher.getName())) throw new IllegalArgumentException("请填写姓名");
        if (isBlank(teacher.getSubject())) throw new IllegalArgumentException("请填写擅长科目");
        if (isBlank(teacher.getGrade())) throw new IllegalArgumentException("请填写可教年级");
        if (teacher.getPricePerHour() == null || teacher.getPricePerHour() <= 0)
            throw new IllegalArgumentException("期望时薪必须大于 0");

        Teacher exist = teacherRepository.findAll().stream()
                .filter(t -> teacher.getUserId().equals(t.getUserId()))
                .findFirst()
                .orElse(null);
        if (exist != null) {
            teacher.setId(exist.getId());
        }
        return teacherRepository.save(teacher);
    }

    // 家长查老师（家长登录时用）
    @GetMapping("/teachers")
    public List<Teacher> searchTeachers(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {

        return teacherRepository.findAll().stream().filter(t -> {
            boolean matchSubject = (subject == null || subject.isEmpty())
                    || (t.getSubject() != null && t.getSubject().contains(subject));
            boolean matchGrade = matchGrade(grade, t.getGrade());
            return matchSubject && matchGrade;
        }).toList();
    }

    // 老师查自己那份资料
    @GetMapping("/teacher/my")
    public Teacher myTeacher(@RequestParam Long userId) {
        return teacherRepository.findAll().stream()
                .filter(t -> t.getUserId() != null && t.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    // ================== 家长需求 ==================

    // 发布需求
    @PostMapping("/parent")
    public Parent saveParent(@RequestBody Parent parent) {
        Long uid = currentUser.id();
        if (uid != null) parent.setUserId(uid);
        if (parent.getUserId() == null) throw new IllegalArgumentException("请先登录");
        if (isBlank(parent.getName())) throw new IllegalArgumentException("请填写您的称呼");
        if (isBlank(parent.getDemandSubject())) throw new IllegalArgumentException("请填写需求科目");
        if (isBlank(parent.getDemandGrade())) throw new IllegalArgumentException("请填写需求年级");
        if (parent.getBudget() == null || parent.getBudget() <= 0)
            throw new IllegalArgumentException("预算时薪必须大于 0");
        parent.setStatus(1);
        parent.setTeacherUserId(null);
        return parentRepository.save(parent);
    }

    // 老师查家长需求（老师登录时用，只返回还在找老师的）
    @GetMapping("/parents")
    public List<Parent> searchParents(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {

        return parentRepository.findAll().stream().filter(p -> {
            // 只要还在找老师的
            if (p.getStatus() != null && p.getStatus() == 0) return false;

            boolean matchSubject = (subject == null || subject.isEmpty())
                    || (p.getDemandSubject() != null && p.getDemandSubject().contains(subject));
            boolean matchGrade = matchGrade(grade, p.getDemandGrade());
            return matchSubject && matchGrade;
        }).toList();
    }

    // 家长查自己发布的需求（含已结束），并统计有多少位老师联系过
    @GetMapping("/my-demands")
    public List<Map<String, Object>> myDemands(@RequestParam Long userId) {
        return parentRepository.findAll().stream()
                .filter(p -> p.getUserId() != null && p.getUserId().equals(userId))
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .map(p -> {
                    long applyCount = messageRepository.findAll().stream()
                            .filter(m -> p.getUserId().equals(m.getToUserId()))
                            .map(m -> m.getFromUserId())
                            .distinct()
                            .count();

                    Map<String, Object> map = new HashMap<>();
                    map.put("demand", p);
                    map.put("applyCount", applyCount);
                    return map;
                })
                .toList();
    }

    // 家长确认结束需求（只有发布者本人能操作）
    @PutMapping("/parents/{id}/finish")
    public Map<String, Object> finishParentDemand(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Long uid = currentUser.id();
        if (uid != null) userId = uid;                    // 家长身份以 token 为准

        Map<String, Object> result = new HashMap<>();
        Parent parent = parentRepository.findById(id).orElse(null);

        if (parent == null) {
            result.put("success", false);
            result.put("message", "需求不存在");
            return result;
        }
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

    // ================== 接单申请 ==================

    // 老师申请接单：只发一条私信，不改需求状态
    @PostMapping("/apply")
    public Map<String, Object> applyDemand(
            @RequestParam Long teacherUserId,
            @RequestParam Long parentDemandId,
            @RequestParam(required = false) String text) {
        Long uid = currentUser.id();
        if (uid != null) teacherUserId = uid;             // 老师身份以 token 为准

        Map<String, Object> result = new HashMap<>();
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

        Message m = new Message();
        m.setFromUserId(teacherUserId);
        m.setFromRole("TEACHER");
        m.setToUserId(parent.getUserId());
        m.setToRole("PARENT");
        m.setContent((text == null || text.isBlank())
                ? "您好，我想接这个家教需求，方便沟通一下吗？"
                : text);
        m.setSendTime(LocalDateTime.now());
        messageRepository.save(m);

        result.put("success", true);
        result.put("message", "申请已发送，等待家长确认");
        return result;
    }

    // ================== 用户信息 ==================

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // ================== 工具方法 ==================

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * 年级匹配：
     * 初中 → 初一/初二/初三；高中 → 高一/高二/高三；其他走模糊包含；空则不筛
     */
    private boolean matchGrade(String searchKeyword, String actualGrade) {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) return true;
        String keyword = searchKeyword.trim();
        if ("初中".equals(keyword)) {
            return "初一".equals(actualGrade) || "初二".equals(actualGrade) || "初三".equals(actualGrade);
        } else if ("高中".equals(keyword)) {
            return "高一".equals(actualGrade) || "高二".equals(actualGrade) || "高三".equals(actualGrade);
        } else {
            return actualGrade != null && actualGrade.contains(keyword);
        }
    }
}