package com.accommodation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Child entity in the JPA Inheritance hierarchy.
 * Mapped to the 'student_persons' table with a foreign key referencing 'persons(id)'.
 */
@Entity
@Table(name = "student_persons")
@PrimaryKeyJoinColumn(name = "person_id")
public class StudentPerson extends Person {

    @Column(name = "roll_number", nullable = false)
    private String rollNumber;

    @Column(name = "course", nullable = false)
    private String course;

    public StudentPerson() {
        super();
    }

    public StudentPerson(String name, String email, String rollNumber, String course) {
        super(name, email);
        this.rollNumber = rollNumber;
        this.course = course;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
