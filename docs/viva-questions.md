# Comprehensive Viva & Technical Interview Questions

This document is prepared to help you confidently explain your **Student Accommodation Management System** project during viva, lab assessments, and technical interviews.

---

## 1. Spring Boot & Spring MVC

### Q1: What is Spring Boot and why did you use it instead of standard Spring Framework?
**Answer:**
Spring Boot is an opinionated extension of the Spring Framework designed to simplify Java development. In traditional Spring, developers had to write extensive XML configurations or Java config classes and manually configure Tomcat/web servers.
Spring Boot provides:
1. **Auto-Configuration:** Automatically configures components based on JAR dependencies present on the classpath (e.g. `spring-boot-starter-web` automatically configs DispatcherServlet and Tomcat).
2. **Starter Dependencies:** Bundles cohesive libraries (e.g., `spring-boot-starter-data-jpa` includes Spring Data, Hibernate, and JDBC pools).
3. **Embedded Servlet Container:** Runs Tomcat embedded on port 8080 without requiring standalone Tomcat installation.
4. **Production-Ready Features:** Health checks, metrics, and application properties.

### Q2: Explain the MVC (Model-View-Controller) architecture in this project.
**Answer:**
- **Model:** Represents the domain data and state. In our project, entities like `Student`, `Room`, `Booking`, and DTOs like `BookingRequestDTO` act as models. Spring's `Model` interface passes data to views.
- **View:** The user interface rendered to the client. We used **Thymeleaf HTML templates** located in `src/main/resources/templates/`.
- **Controller:** Intercepts incoming HTTP requests, invokes service logic, updates the Model, and returns the appropriate View name or REST response. Annotated with `@Controller` (for web views) and `@RestController` (for JSON APIs).

### Q3: What is the request flow when a user opens the dashboard?
**Answer:**
1. Browser requests `GET http://localhost:8080/dashboard`.
2. Spring's **`DispatcherServlet`** receives the request and consults HandlerMapping.
3. HandlerMapping directs the request to `DashboardController.viewDashboard(Model model)`.
4. `DashboardController` calls `StudentService`, `RoomService`, and `BookingService` to fetch counts and recent records.
5. Service methods are intercepted by `LoggingAspect` (Spring AOP), which logs method duration.
6. Services call `StudentRepository`, `RoomRepository`, and `BookingRepository` (Spring Data JPA) to query PostgreSQL.
7. The controller populates the `Model` object with statistics and returns `"dashboard"`.
8. Thymeleaf View Resolver renders `dashboard.html` with data into an HTML page and sends it to the browser.

---

## 2. Dependency Injection & Spring Core Concepts

### Q4: What is Inversion of Control (IoC) and Dependency Injection (DI)?
**Answer:**
- **Inversion of Control (IoC):** A software design principle where the control of object creation, configuration, and lifecycle is inverted from the program code itself to an external framework or container (the Spring IoC Container).
- **Dependency Injection (DI):** The design pattern used by Spring to implement IoC. Instead of a class creating its own dependencies with `new StudentRepositoryImpl()`, the container injects the required dependency into the class.

### Q5: What is the difference between BeanFactory and ApplicationContext?
**Answer:**
- **`BeanFactory`:** The basic Spring IoC container interface. It supports lazy loading (beans are instantiated only when requested via `getBean()`). It is lightweight and suited for memory-constrained environments.
- **`ApplicationContext`:** A sub-interface of `BeanFactory` that adds enterprise-grade capabilities: eager bean pre-instantiation at startup, internationalization (i18n), event publication, and seamless integration with Spring AOP and web environments. Spring Boot uses `ApplicationContext`.

### Q6: Why did you use Constructor-based Dependency Injection instead of `@Autowired` on fields?
**Answer:**
1. **Immutability:** Dependencies can be declared `final`, ensuring thread safety and that dependencies cannot be modified after object construction.
2. **Ease of Unit Testing:** We can easily pass mock objects directly via the constructor without needing reflection or Spring container startup (e.g. `new StudentService(mockRepository)` in JUnit).
3. **Fail-Fast Safety:** Prevents `NullPointerException` because the class cannot be instantiated without its mandatory dependencies.

### Q7: Explain the difference between `@Component`, `@Service`, `@Repository`, `@Controller`, and `@RestController`.
**Answer:**
- `@Component`: Generic stereotype annotation for any Spring-managed bean.
- `@Service`: Specialization of `@Component` for classes holding business logic.
- `@Repository`: Specialization of `@Component` for data access classes; automatically translates database vendor exceptions into Spring's unified `DataAccessException` hierarchy.
- `@Controller`: Specialization of `@Component` for web MVC controllers returning view templates (HTML).
- `@RestController`: A combination of `@Controller` and `@ResponseBody`. Tells Spring to serialize returned Java objects directly into JSON/XML HTTP response bodies rather than resolving an HTML view.

---

## 3. JPA, Hibernate, & Database Connectivity

### Q8: What is the difference between JPA and Hibernate?
**Answer:**
JPA is a **specification** (a standardized API of Java interfaces and annotations), whereas Hibernate is a concrete **implementation** of the JPA specification. In our project, we write JPA annotations like `@Entity`, `@Id`, `@Table`, and Spring Boot uses Hibernate under the hood to perform the SQL generation and object-relational mapping.

### Q9: Explain `@Entity`, `@Id`, and `GenerationType.IDENTITY`.
**Answer:**
- `@Entity`: Marks the class as a persistent database entity managed by Hibernate.
- `@Id`: Designates the primary key attribute.
- `GenerationType.IDENTITY`: Tells the persistence provider to rely on PostgreSQL's auto-incrementing identity column (`SERIAL` / `BIGSERIAL`) to assign IDs upon row insertion.

### Q10: What is HQL (Hibernate Query Language) and how is it different from SQL?
**Answer:**
- **SQL** queries direct database **tables and columns** (e.g., `SELECT * FROM students WHERE dept = 'CS'`).
- **HQL** queries Java **Entity classes and entity properties** (e.g., `SELECT s FROM Student s WHERE s.department = :dept`).
- HQL is database-independent: Hibernate translates the HQL query into the specific SQL dialect of the underlying database (PostgreSQL in our case).

### Q11: Explain the two HQL queries in your project.
**Answer:**
1. **Query 1 (Students by Department):**
   ```java
   @Query("SELECT s FROM Student s WHERE LOWER(s.department) = LOWER(:dept)")
   List<Student> findStudentsByDepartmentHql(@Param("dept") String dept);
   ```
   *Explanation:* Filters `Student` entity records where the property `department` matches the parameter `dept`.
2. **Query 2 (Active Bookings for a Student):**
   ```java
   @Query("SELECT b FROM Booking b WHERE b.student.id = :studentId AND b.status = 'ACTIVE'")
   List<Booking> findActiveBookingsByStudentHql(@Param("studentId") Long studentId);
   ```
   *Explanation:* Navigates the object relationship `b.student.id` to find active booking records for a given student without writing raw SQL joins.

### Q12: Explain JPA Inheritance Mapping and the `JOINED` strategy used in your project.
**Answer:**
We modeled a `Person` base entity and a `StudentPerson` subclass:
- Parent: `@Inheritance(strategy = InheritanceType.JOINED) public class Person` (holds `id`, `name`, `email`).
- Child: `public class StudentPerson extends Person` (holds `rollNumber`, `course`).
- In the database, Hibernate generates two separate tables: `persons` and `student_persons`.
- The `student_persons` table shares the same primary key value as `persons.id`, functioning simultaneously as a primary key and a foreign key (`@PrimaryKeyJoinColumn(name = "person_id")`).
- This avoids duplicate columns and prevents null values that occur in Single Table inheritance.

### Q13: What is `@Transactional` and why is it essential for booking rooms?
**Answer:**
Booking an accommodation room involves multiple database operations:
1. Validating student and room availability.
2. Incrementing `room.occupiedBeds` and updating availability status.
3. Inserting the new `Booking` record.
If an error occurs midway (e.g. database constraint violation), `@Transactional` guarantees **Atomicity (the A in ACID)**: either all operations succeed and commit, or all operations roll back, preventing inconsistent data such as an incremented room occupancy with no corresponding booking record.

---

## 4. Spring AOP (Aspect-Oriented Programming)

### Q14: What is Spring AOP and what problem does it solve?
**Answer:**
Spring AOP enables modularizing **cross-cutting concerns**—features like logging, security, and transaction management that span across multiple layers of an application. Instead of copy-pasting logging statements into every service method, AOP encapsulates the logging logic in an `@Aspect` class.

### Q15: Explain `@Aspect`, `@Pointcut`, and `@Around` advice in your project.
**Answer:**
In `LoggingAspect.java`:
- `@Aspect`: Identifies the class as an Aspect containing cross-cutting logic.
- `@Pointcut("execution(* com.accommodation.service.*.*(..))")`: Defines the join points—all methods inside `com.accommodation.service` package.
- `@Around`: The most powerful advice type. It wraps the target method call using `ProceedingJoinPoint.proceed()`. We record `System.currentTimeMillis()` before and after method invocation to measure and log method execution time and catch exceptions.

---

## 5. REST APIs

### Q16: What is a REST API and what HTTP methods did you implement?
**Answer:**
REST (Representational State Transfer) is an architectural style for networked applications using standard HTTP operations:
- **`GET`**: Retrieve resources (idempotent, safe). E.g., `GET /api/students`, `GET /api/rooms/available`.
- **`POST`**: Create new resources. E.g., `POST /api/students`, `POST /api/bookings`.
- **`PUT`**: Update or mutate existing resources. E.g., `PUT /api/students/{id}`, `PUT /api/bookings/{id}/cancel`.
- **`DELETE`**: Remove resources. E.g., `DELETE /api/students/{id}`.

### Q17: What is the difference between REST and SOAP?
**Answer:**
| Feature | REST | SOAP |
| :--- | :--- | :--- |
| **Protocol** | Architectural style; uses HTTP directly. | Standardized XML messaging protocol. |
| **Data Format** | Primarily lightweight JSON; also supports XML, HTML. | Strict XML only. |
| **Payload Size** | Lightweight and fast. | Heavy due to XML envelopes and headers. |
| **Contract** | Optional OpenAPI/Swagger specs. | Strict WSDL (Web Services Description Language). |

---

## 6. Microservices & Distributed Architecture

### Q18: What is the difference between Monolithic and Microservices architecture?
**Answer:**
- **Monolith:** All functionality (student management, rooms, bookings, notifications, reports) is packaged and deployed as a single application unit.
- **Microservices:** The application is split into small, independently deployable services that focus on specific business capabilities and communicate over lightweight protocols (such as HTTP/REST).

### Q19: How did you implement Microservices in this project?
**Answer:**
We built two separate Spring Boot projects:
1. **`accommodation-main` (Port 8080):** Manages students, rooms, database persistence, and web UI.
2. **`notification-service` (Port 8081):** Dedicated microservice exposing `POST /api/notifications/booking` to process simulated student booking confirmations.
When a booking is confirmed on port 8080, `NotificationClientService` makes an HTTP POST request to port 8081 using Spring's `RestClient`.

### Q20: What happens if the Notification Service is offline?
**Answer:**
We implemented **graceful degradation / fault tolerance**:
The HTTP call to the notification microservice is enclosed in a `try-catch` block. If the notification service is down (e.g. connection refused), the exception is logged as a warning, and the booking transaction on port 8080 completes and commits successfully without crashing or rolling back the student's booking.

### Q21: What is Spring Cloud and how would it be used in production?
**Answer:**
In large-scale enterprise deployments, microservices require infrastructure management provided by **Spring Cloud**:
1. **Service Discovery (Eureka / Consul):** Allows microservices to dynamically locate each other by service name instead of hardcoded hostnames and ports.
2. **API Gateway (Spring Cloud Gateway):** Single entry point that routes external client requests, handles SSL termination, rate limiting, and authentication.
3. **Centralized Configuration (Spring Cloud Config):** Externalizes configuration properties for all microservices in a Git repository.
4. **Resilience / Circuit Breaker (Resilience4j):** Prevents cascading system failures if a downstream microservice suffers an outage.

---

## 7. Maven, Git, DevOps, & CI/CD

### Q22: What is Maven and what role does `pom.xml` play?
**Answer:**
Apache Maven is a build automation and dependency management tool for Java projects.
`pom.xml` (Project Object Model) defines:
- Project coordinates (`groupId`, `artifactId`, `version`).
- Dependencies and versions required by the application.
- Build plugins (e.g. `spring-boot-maven-plugin` packages the executable JAR; `maven-surefire-plugin` runs JUnit tests).

### Q23: What is Git and GitHub?
**Answer:**
- **Git:** A distributed version control system that tracks source code changes locally, supports branching, commits, and rollbacks.
- **GitHub:** A cloud-based hosting platform for Git repositories that enables collaborative development, pull requests, issue tracking, and automation.

### Q24: What is DevOps and CI/CD?
**Answer:**
- **DevOps:** A cultural and technical practice combining Development (Dev) and Operations (Ops) to shorten the software development lifecycle and deliver high-quality features continuously.
- **CI (Continuous Integration):** Developers frequently merge code into a shared repository, where automated builds and tests verify the changes (e.g. using GitHub Actions on every `git push`).
- **CD (Continuous Delivery / Continuous Deployment):** Automatically packaging and deploying verified builds to staging or production environments.
