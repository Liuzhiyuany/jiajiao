package com.example.jiajiao.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;               // 主键（需求编号）

    private Integer status = 1;    // 1=找老师中，0=已结束
    private String name;           // 家长称呼
    private String demandSubject;  // 需求科目
    private String demandGrade;    // 需求年级
    private Double budget;         // 预算时薪
    private String contactPhone;   // 联系电话
    private String requirement;    // 具体要求
    private String location;       // 家庭所在区域
    private String teachMode;      // 线上/线下
    private Long userId;           // 发布需求的家长账号ID
    private Long teacherUserId;    // 接单老师的账号ID（新增）

    public Parent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDemandSubject() { return demandSubject; }
    public void setDemandSubject(String demandSubject) { this.demandSubject = demandSubject; }

    public String getDemandGrade() { return demandGrade; }
    public void setDemandGrade(String demandGrade) { this.demandGrade = demandGrade; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTeachMode() { return teachMode; }
    public void setTeachMode(String teachMode) { this.teachMode = teachMode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTeacherUserId() { return teacherUserId; }
    public void setTeacherUserId(Long teacherUserId) { this.teacherUserId = teacherUserId; }
}