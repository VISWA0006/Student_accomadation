package com.accommodation.repository;

import com.accommodation.entity.StudentPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentPersonRepository extends JpaRepository<StudentPerson, Long> {
}
