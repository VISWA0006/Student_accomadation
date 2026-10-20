package com.accommodation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StudentDTO {

    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Register number is required")
    private String registerNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Year of study is required")
    private Integer yearOfStudy;

    @NotBlank(message = "Gender is required")
    private String gender = "MALE";

    public StudentDTO() {
    }

    public StudentDTO(Long id, String fullName, String registerNumber, String email, String phoneNumber, String department, Integer yearOfStudy) {
        this(id, fullName, registerNumber, email, phoneNumber, department, yearOfStudy, "MALE");
    }

    public StudentDTO(Long id, String fullName, String registerNumber, String email, String phoneNumber, String department, Integer yearOfStudy, String gender) {
        this.id = id;
        this.fullName = fullName;
        this.registerNumber = registerNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.gender = gender != null ? gender.trim().toUpperCase() : "MALE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender != null ? gender.trim().toUpperCase() : "MALE";
    }
}
