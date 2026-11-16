package com.example.jiajiao.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 家长的唯一编号
    private String name; // 家长称呼
    private String demandSubject; // 需求科目（如：数学）
    private String demandGrade; // 需求年级（如：初二）
    private Double budget; // 预算（时薪）
    private String contactPhone; // 联系电话
    private String requirement;

    public Parent() {}
    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
}