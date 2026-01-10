# Microservice Perpustakaan

Sistem manajemen perpustakaan berbasis microservice architecture dengan Spring Boot.

---

## Arsitektur

- **anggota** (Port 8081) - Member Service
- **buku** (Port 8082) - Book Service  
- **peminjaman** (Port 8083) - Borrowing Service
- **pengembalian** (Port 8084) - Return Service

---

## Teknologi yang Diterapkan

1. **CQRS (Command Query Responsibility Segregation)**
2. **RabbitMQ Event-Driven Architecture**
3. **Structured Logging (ELK-Ready)**
4. **Distributed Tracing (Micrometer Tracing)**
5. **Spring Boot Actuator Monitoring**
6. **Jenkins CI/CD**

---

## Prerequisites

- Java 17
- Maven 3.6+
- RabbitMQ Server
- Jenkins

---

## Quick Start

```bash
# Build
mvn clean package

# Run
cd anggota && mvn spring-boot:run
cd buku && mvn spring-boot:run
cd peminjaman && mvn spring-boot:run
cd pengembalian && mvn spring-boot:run
```

---