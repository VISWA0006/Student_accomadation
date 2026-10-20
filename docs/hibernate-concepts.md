# Hibernate, JPA, and ORM Concepts Guide

This guide details the Object-Relational Mapping (ORM) and persistence architecture implemented in the **Student Accommodation Management System**.

---

## 1. JPA vs. Hibernate

* **JPA (Jakarta Persistence API)**: A **specification** (set of interfaces and rules) that defines standard object-relational mapping in Java. Annotations like `@Entity`, `@Table`, `@Id`, `@Column`, `@OneToMany`, `@ManyToOne`, and the `EntityManager` interface are part of the JPA standard.
* **Hibernate**: An open-source, high-performance **implementation** (ORM provider) of the JPA specification. Hibernate implements the interfaces defined by JPA and adds extra features such as caching, dirty checking, and HQL.

> In Spring Boot, our application targets JPA standard interfaces, and Hibernate runs as the default JPA vendor under the hood.

---

## 2. Core Persistence Architecture

### A. SessionFactory vs. EntityManagerFactory
* **`SessionFactory` (Hibernate-native)**: A thread-safe, immutable cache of compiled mappings for a single database. Creating a SessionFactory is expensive; typically, only one exists per application.
* **`EntityManagerFactory` (JPA Standard)**: The JPA equivalent of SessionFactory. When running Spring Boot with Hibernate, Spring creates a `LocalContainerEntityManagerFactoryBean` that wraps Hibernate's `SessionFactory`.

### B. Session vs. EntityManager
* **`Session` (Hibernate-native)**: The primary interface used by Hibernate applications to create, read, and delete persistent objects. It represents a single-threaded unit of work and wraps a JDBC connection.
* **`EntityManager` (JPA Standard)**: The JPA equivalent of Session. Spring Boot injects an `EntityManager` proxy that manages session lifecycles automatically based on active transactions.

### C. Transaction
* A **Transaction** represents a single atomic unit of database operations adhering to ACID principles (Atomicity, Consistency, Isolation, Durability).
* In Spring, `@Transactional` automatically begins, commits, or rolls back transactions when runtime exceptions occur.

---

## 3. ORM Mapping & Entity Relationships

### A. Mapping Annotations
* `@Entity`: Marks a POJO as a persistent domain entity.
* `@Table(name = "...")`: Maps the class to a specific relational database table.
* `@Id`: Specifies the primary key of the entity.
* `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Relies on PostgreSQL's auto-incrementing `SERIAL` or identity column.
* `@Column`: Maps an attribute to a table column, specifying constraints like `nullable = false` or `unique = true`.

### B. Entity Relationships in this Project
1. **Student to Booking (`@OneToMany`)**:
   - One student can have multiple accommodation booking records across semesters.
   - Code: `List<Booking> bookings` in `Student.java` mapped by `student`.
2. **Booking to Student (`@ManyToOne`)**:
   - Each booking record belongs to exactly one student.
   - Foreign key: `student_id` in `bookings` table.
3. **Booking to Room (`@ManyToOne`)**:
   - Each booking allocates one room.
   - Foreign key: `room_id` in `bookings` table.

---

## 4. Primary Key Generation Strategies

| Strategy | Description | Typical Database |
| :--- | :--- | :--- |
| **`IDENTITY`** *(Used)* | Database auto-increments the primary key upon row insert. | PostgreSQL (`SERIAL`/`IDENTITY`), MySQL (`AUTO_INCREMENT`). |
| **`SEQUENCE`** | Uses a database sequence object to generate unique values prior to insert. | PostgreSQL, Oracle. |
| **`TABLE`** | Uses an auxiliary database table to maintain and simulate sequence counters. | Universal (slower performance). |
| **`AUTO`** | Lets JPA provider select the strategy based on database dialect. | Provider dependent. |

---

## 5. HQL (Hibernate Query Language) vs. SQL

| Aspect | SQL (Structured Query Language) | HQL (Hibernate Query Language) |
| :--- | :--- | :--- |
| **Target** | Direct database **tables and columns**. | Java **Entity classes and attributes**. |
| **Case Sensitivity** | Mostly case-insensitive (depending on RDBMS). | Entity names and fields are **case-sensitive** matching Java code. |
| **Portability** | Database-specific dialects (e.g. Postgres vs Oracle vs MySQL). | Highly portable; Hibernate translates HQL into target database SQL automatically. |
| **Associations** | Explicit SQL `JOIN` clauses on foreign key columns. | Object path navigation (e.g. `b.student.id`). |

### HQL Examples Implemented in this Project:

#### 1. Find Students by Department:
```java
@Query("SELECT s FROM Student s WHERE LOWER(s.department) = LOWER(:dept)")
List<Student> findStudentsByDepartmentHql(@Param("dept") String dept);
```

#### 2. Find Active Bookings for a Student:
```java
@Query("SELECT b FROM Booking b WHERE b.student.id = :studentId AND b.status = 'ACTIVE'")
List<Booking> findActiveBookingsByStudentHql(@Param("studentId") Long studentId);
```

---

## 6. Inheritance Mapping: Joined Table Strategy

Demonstrated via the `Person` and `StudentPerson` classes:

```java
@Entity
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
public class Person {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
}

@Entity
@Table(name = "student_persons")
@PrimaryKeyJoinColumn(name = "person_id")
public class StudentPerson extends Person {
    private String rollNumber;
    private String course;
}
```

### Database Schema Created:
* **`persons` table**: Contains `id` (PK), `name`, `email`.
* **`student_persons` table**: Contains `person_id` (PK & FK referencing `persons.id`), `roll_number`, `course`.

### Advantage:
- Normalized relational design: No redundant columns or null values.
- Clean object-oriented inheritance modeling in Java.
