package com.example.jiajiao.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String subject;
    private String grade;
    private Double pricePerHour;
    private String intro;
    private String contactPhone;
    private String school; // 新加的学校字段
    private String location;  // 授课区域
    private String teachMode; // 线上/线下

    public Teacher() {}

    // 加上对应的 Get/Set 方法
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTeachMode() { return teachMode; }
    public void setTeachMode(String teachMode) { this.teachMode = teachMode; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
}