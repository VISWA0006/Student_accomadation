package com.accommodation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Register number is required")
    @Column(name = "register_number", nullable = false, unique = true)
    private String registerNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @NotBlank(message = "Department is required")
    @Column(name = "department", nullable = false)
    private String department;

    @NotNull(message = "Year of study is required")
    @Column(name = "year_of_study", nullable = false)
    private Integer yearOfStudy;

    @NotBlank(message = "Gender is required")
    @Column(name = "gender", nullable = false)
    private String gender = "MALE";

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    public Student() {
    }

    public Student(String fullName, String registerNumber, String email, String phoneNumber, String department, Integer yearOfStudy) {
        this(fullName, registerNumber, email, phoneNumber, department, yearOfStudy, "MALE");
    }

    public Student(String fullName, String registerNumber, String email, String phoneNumber, String department, Integer yearOfStudy, String gender) {
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

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", registerNumber='" + registerNumber + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", yearOfStudy=" + yearOfStudy +
                ", gender='" + gender + '\'' +
                '}';
    }
}
