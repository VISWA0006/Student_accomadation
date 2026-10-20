package com.accommodation.repository;

import com.accommodation.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRegisterNumber(String registerNumber);

    boolean existsByRegisterNumber(String registerNumber);

    // Simple search by name or register number
    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> searchStudents(@Param("keyword") String keyword);

    /**
     * Demonstrates Hibernate Query Language (HQL) using @Query.
     * References the entity name 'Student' and entity property 'department'.
     */
    @Query("SELECT s FROM Student s WHERE LOWER(s.department) = LOWER(:dept)")
    List<Student> findStudentsByDepartmentHql(@Param("dept") String dept);
}
