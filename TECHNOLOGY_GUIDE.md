# 📖 Panduan Lengkap Teknologi - Microservice Perpustakaan

## Daftar Isi

1. [CQRS](#1-cqrs-command-query-responsibility-segregation)
2. [RabbitMQ Event-Driven](#2-rabbitmq-event-driven-architecture)
3. [Structured Logging](#3-structured-logging-elk-ready)
4. [Distributed Tracing](#4-distributed-tracing)
5. [Actuator Monitoring](#5-spring-boot-actuator-monitoring)
6. [Jenkins CI/CD](#6-jenkins-cicd)

---

## 1. CQRS (Command Query Responsibility Segregation)

### 📌 Pengertian

CQRS adalah pattern yang memisahkan operasi **write (Command)** dan **read (Query)** menjadi model yang berbeda.

### 🎯 Tujuan Implementasi

- Pemisahan tanggung jawab antara write dan read
- Optimasi performa untuk query yang kompleks
- Scalability yang lebih baik

### 📂 Lokasi Implementasi

**Service:** `peminjaman` (Port 8083)

**Struktur folder:**
```
peminjaman/src/main/java/com/pail/peminjaman/application/
├── Command.java                    # Interface untuk semua command
├── Query.java                      # Interface untuk semua query
├── CommandHandler.java             # Interface handler command
├── QueryHandler.java               # Interface handler query
├── commands/
│   ├── CreatePeminjamanCommand.java
│   ├── CreatePeminjamanHandler.java
│   ├── UpdatePeminjamanCommand.java
│   ├── UpdatePeminjamanHandler.java
│   ├── DeletePeminjamanCommand.java
│   └── DeletePeminjamanHandler.java
└── queries/
    ├── GetAllPeminjamanQuery.java
    ├── GetAllPeminjamanHandler.java
    ├── GetPeminjamanByIdQuery.java
    ├── GetPeminjamanByIdHandler.java
    ├── GetPeminjamanWithDetailsQuery.java
    └── GetPeminjamanWithDetailsHandler.java
```

### 💻 Cara Kerja

**1. Command (Write Operation):**
```java
// Controller menerima request
@PostMapping
public ResponseEntity<CommandResult> createPeminjaman(@RequestBody PeminjamanModel peminjaman) {
    // Buat command object
    CreatePeminjamanCommand command = new CreatePeminjamanCommand(peminjaman);
    
    // Kirim ke handler
    CommandResult result = createHandler.handle(command);
    
    // Return result
    return ResponseEntity.status(201).body(result);
}
```

**Response Command:**
```json
{
  "id": 1,
  "success": true,
  "message": "Peminjaman created successfully"
}
```

**2. Query (Read Operation):**
```java
// Controller menerima request
@GetMapping("/anggota/{id}")
public List<ResponseTemplate> getPeminjamanWithAnggota(@PathVariable Long id) {
    // Buat query object
    GetPeminjamanWithDetailsQuery query = new GetPeminjamanWithDetailsQuery(id);
    
    // Kirim ke handler
    List<ResponseTemplate> result = getWithDetailsHandler.handle(query);
    
    // Return data lengkap
    return result;
}
```

**Response Query:**
```json
[
  {
    "peminjaman": {
      "id": 1,
      "tanggal_pinjam": "2024-12-25",
      "tanggal_kembali": "2025-01-01"
    },
    "anggota": {
      "id": 1,
      "nama": "John Doe"
    },
    "buku": {
      "id": 1,
      "judul": "Spring Boot Guide"
    }
  }
]
```

### 🧪 Testing CQRS

```bash
# Test COMMAND - Create
curl -X POST http://localhost:8083/api/peminjaman \
  -H "Content-Type: application/json" \
  -d '{
    "anggotaId": 1,
    "bukuId": 1,
    "tanggal_pinjam": "2024-12-25",
    "tanggal_kembali": "2025-01-01"
  }'

# Expected: {"id":1,"success":true,"message":"Peminjaman created successfully"}

# Test QUERY - Read
curl http://localhost:8083/api/peminjaman/anggota/1

# Expected: [{peminjaman:{...}, anggota:{...}, buku:{...}}]
```

### ✅ Keuntungan

- **Separation of Concerns:** Logic write dan read terpisah
- **Scalability:** Bisa di-scale terpisah antara write dan read
- **Performance:** Query bisa dioptimasi tanpa ganggu write
- **Flexibility:** Mudah tambah query baru tanpa ubah command

---

## 2. RabbitMQ Event-Driven Architecture

### 📌 Pengertian

RabbitMQ adalah message broker untuk komunikasi asynchronous antar service menggunakan event.

### 🎯 Tujuan Implementasi

- Decoupling antar service
- Asynchronous processing
- Email notification otomatis saat peminjaman dibuat

### 📂 Lokasi Implementasi

**Producer:**
- `peminjaman/service/RabbitMQProducerService.java`
- `pengembalian/service/RabbitMQProducerService.java`

**Consumer:**
- `peminjaman/service/RabbitMQConsumerService.java`

**Configuration:**
- `peminjaman/config/RabbitMQConfig.java`
- `pengembalian/config/RabbitMQConfig.java`

### 💻 Cara Kerja

**1. Publish Event (Producer):**
```java
@Service
public class RabbitMQProducerService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishPeminjamanEvent(PeminjamanEventDTO event) {
        // Publish ke exchange
        rabbitTemplate.convertAndSend(
            "library_exchange",      // Exchange name
            "library_routing_key",   // Routing key
            event                     // Event object
        );
    }
}
```

**2. Consume Event (Consumer):**
```java
@Service
public class RabbitMQConsumerService {
    
    @Autowired
    private EmailService emailService;
    
    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void handlePeminjamanEvent(PeminjamanEventDTO event) {
        // Process event
        emailService.sendEmail(event);
    }
}
```

### 🔄 Event Flow

```
1. User POST → /api/peminjaman
        ↓
2. CreatePeminjamanHandler
   - Save ke database
   - Publish event ke RabbitMQ
        ↓
3. RabbitMQ (library_exchange)
   - Route ke library_queue
        ↓
4. RabbitMQConsumerService
   - Consume event dari queue
        ↓
5. EmailService
   - Send email notification
```

### ⚙️ Konfigurasi

**application.yml:**
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

app:
  rabbitmq:
    exchange: library_exchange
    routingkey: library_routing_key
    queue: library_queue
```

**RabbitMQConfig.java:**
```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue queue() {
        return new Queue("library_queue", true);
    }
    
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("library_exchange");
    }
    
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with("library_routing_key");
    }
}
```

### 🧪 Testing RabbitMQ

**1. Start RabbitMQ:**
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management-alpine
```

**2. Access Management UI:**
```
http://localhost:15672
Username: guest
Password: guest
```

**3. Create Peminjaman:**
```bash
curl -X POST http://localhost:8083/api/peminjaman \
  -H "Content-Type: application/json" \
  -d '{
    "anggotaId": 1,
    "bukuId": 1,
    "tanggal_pinjam": "2024-12-25",
    "tanggal_kembali": "2025-01-01"
  }'
```

**4. Check di RabbitMQ Management:**
- Queues → library_queue
- Lihat message diterima dan di-consume

### ✅ Keuntungan

- **Decoupling:** Service tidak perlu tahu siapa konsumen eventnya
- **Asynchronous:** Tidak block main process
- **Reliability:** Message disimpan di queue sampai berhasil diproses
- **Scalability:** Bisa tambah consumer untuk load balancing

---

## 3. Structured Logging (ELK-Ready)

### 📌 Pengertian

Structured logging adalah format log terstruktur dengan key-value pairs, siap untuk diintegrasikan dengan ELK Stack (Elasticsearch, Logstash, Kibana).

### 🎯 Tujuan Implementasi

- Log yang mudah di-parse dan di-search
- Ready untuk monitoring dengan ELK Stack
- Correlation ID untuk tracking request

### 📂 Lokasi Implementasi

**Semua service:**
- `src/main/resources/logback-spring.xml`
- `src/main/java/com/pail/*/config/LoggingFilter.java`
- Controller classes (structured log statements)

### 💻 Cara Kerja

**1. LoggingFilter:**
```java
@Component
public class LoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        // Generate/extract correlation ID
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Set ke MDC (Mapped Diagnostic Context)
        MDC.put("correlationId", correlationId);
        MDC.put("serviceName", serviceName);
        MDC.put("requestUri", request.getRequestURI());
        MDC.put("httpMethod", request.getMethod());
        
        // Add to response header
        response.setHeader("X-Correlation-ID", correlationId);
        
        filterChain.doFilter(request, response);
        
        // Clear MDC
        MDC.clear();
    }
}
```

**2. Structured Log Statement:**
```java
import static net.logstash.logback.argument.StructuredArguments.kv;

@RestController
public class AnggotaController {
    
    @GetMapping
    public List<AnggotaModel> getAllAnggota() {
        log.info("Request received", kv("action", "GET_ALL"));
        
        List<AnggotaModel> result = service.getAllAnggota();
        
        log.info("Request completed", 
            kv("action", "GET_ALL"),
            kv("status", "SUCCESS"),
            kv("count", result.size())
        );
        
        return result;
    }
}
```

**3. Logback Configuration:**
```xml
<configuration>
    <!-- JSON Appender for ELK -->
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>correlationId</includeMdcKeyName>
            <includeMdcKeyName>serviceName</includeMdcKeyName>
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>
    
    <!-- Readable Console for Development -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %highlight(%-5level) [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

### 📊 Output Log

**Development Mode (Readable):**
```
01:54:15.123 INFO  [abc123,span1] c.p.a.controller.AnggotaController - Request received action=GET_ALL
01:54:15.234 INFO  [abc123,span1] c.p.a.controller.AnggotaController - Request completed action=GET_ALL status=SUCCESS count=5
```

**ELK Mode (JSON):**
```json
{
  "@timestamp": "2025-12-25T01:54:15.123+0700",
  "@version": "1",
  "message": "Request received",
  "logger_name": "com.naufal.anggota.controller.AnggotaController",
  "level": "INFO",
  "correlationId": "abc123-def456",
  "serviceName": "anggota",
  "traceId": "abc123def456",
  "spanId": "span1",
  "action": "GET_ALL"
}
```

### 🧪 Testing Structured Logging

**1. Run service:**
```bash
cd anggota && mvn spring-boot:run
```

**2. Make request:**
```bash
curl http://localhost:8081/api/anggota
```

**3. Check console output:**
- Lihat structured fields: `action=GET_ALL status=SUCCESS`
- Lihat traceId dan spanId di dalam `[]`

**4. Test dengan Correlation ID:**
```bash
curl -H "X-Correlation-ID: my-custom-id" http://localhost:8081/api/anggota
```

Response header akan berisi `X-Correlation-ID: my-custom-id`

### ✅ Keuntungan

- **Searchable:** Mudah search berdasarkan action, status, dll
- **Traceable:** Correlation ID untuk tracking request
- **ELK-Ready:** Langsung bisa diintegrasikan dengan Logstash
- **Debugging:** Mudah debug dengan structured information

---

## 4. Distributed Tracing

### 📌 Pengertian

Distributed tracing adalah teknik untuk tracking request flow across multiple services menggunakan trace ID yang sama.

### 🎯 Tujuan Implementasi

- Track request dari service ke service
- Identifikasi bottleneck performance
- Debug masalah cross-service

### 📂 Lokasi Implementasi

**Dependencies (semua service):**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 💻 Cara Kerja

**Micrometer Tracing otomatis:**
1. Generate **traceId** unik untuk setiap request
2. Generate **spanId** untuk setiap operasi dalam trace
3. Propagate traceId ke service lain via HTTP headers
4. Inject traceId/spanId ke MDC untuk logging

**Propagation Flow:**
```
Request → Peminjaman Service (traceId: abc123, spanId: span1)
    ↓ HTTP call
Anggota Service (traceId: abc123, spanId: span2)
    ↓ HTTP call
Buku Service (traceId: abc123, spanId: span3)
```

### ⚙️ Konfigurasi

**application.properties:**
```properties
# Tracing configuration
management.tracing.sampling.probability=1.0
management.zipkin.tracing.enabled=false

# Logging with trace-id
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

**Penjelasan:**
- `sampling.probability=1.0` → Sample 100% request (untuk development)
- `zipkin.enabled=false` → Tidak kirim ke Zipkin server (log-only mode)
- `%X{traceId:-}` → Tampilkan traceId di log
- `%X{spanId:-}` → Tampilkan spanId di log

### 📊 Output Log

**Service Peminjaman:**
```
01:54:20.100 INFO [peminjaman,abc123def456,span1] - Command received action=CREATE
01:54:20.200 INFO [peminjaman,abc123def456,span2] - Calling Anggota Service
```

**Service Anggota:**
```
01:54:20.210 INFO [anggota,abc123def456,span3] - Request received action=GET_BY_ID id=1
01:54:20.220 INFO [anggota,abc123def456,span3] - Request completed status=SUCCESS
```

**Service Buku:**
```
01:54:20.250 INFO [buku,abc123def456,span4] - Request received action=GET_BY_ID id=1
01:54:20.260 INFO [buku,abc123def456,span4] - Request completed status=SUCCESS
```

**Perhatikan:**
- **traceId sama** (`abc123def456`) di semua service ✅
- **spanId berbeda** (`span1`, `span3`, `span4`) untuk tiap operasi ✅

### 🧪 Testing Distributed Tracing

**1. Start semua service:**
```bash
# Terminal 1
cd anggota && mvn spring-boot:run

# Terminal 2
cd buku && mvn spring-boot:run

# Terminal 3
cd peminjaman && mvn spring-boot:run
```

**2. Create peminjaman (akan call anggota + buku):**
```bash
curl -X POST http://localhost:8083/api/peminjaman \
  -H "Content-Type: application/json" \
  -d '{
    "anggotaId": 1,
    "bukuId": 1,
    "tanggal_pinjam": "2024-12-25",
    "tanggal_kembali": "2025-01-01"
  }'
```

**3. Check console output:**
- Peminjaman console → Lihat traceId (misal: `abc123`)
- Anggota console → traceId yang SAMA (`abc123`)
- Buku console → traceId yang SAMA (`abc123`)

### ✅ Keuntungan

- **End-to-end visibility:** Bisa lihat flow request antar service
- **Performance analysis:** Tahu service mana yang lambat
- **Debugging:** Mudah tracking error across services
- **No external dependency:** Tidak perlu Zipkin server untuk log-only mode

---

## 5. Spring Boot Actuator Monitoring

### 📌 Pengertian

Spring Boot Actuator menyediakan endpoints untuk monitoring dan management aplikasi secara real-time.

### 🎯 Tujuan Implementasi

- Health check untuk setiap service
- Metrics untuk performance monitoring
- HTTP request statistics

### 📂 Lokasi Implementasi

**Dependencies (semua service):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### ⚙️ Konfigurasi

**application.properties:**
```properties
# Actuator Monitoring Endpoints
management.endpoints.web.exposure.include=health,metrics,httpexchanges
management.endpoint.health.show-details=always
management.metrics.tags.application=${spring.application.name}
```

**Penjelasan:**
- `exposure.include` → Endpoint yang di-expose
- `show-details=always` → Tampilkan detail health check
- `metrics.tags.application` → Tag metrics dengan nama service

### 📊 Actuator Endpoints

| Endpoint | URL | Description |
|----------|-----|-------------|
| Health | `/actuator/health` | Status kesehatan service |
| Metrics List | `/actuator/metrics` | Daftar semua metrics |
| HTTP Requests | `/actuator/metrics/http.server.requests` | Statistik HTTP requests |

### 💻 Response Examples

**1. Health Check:**
```bash
curl http://localhost:8081/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 994662584320,
        "free": 450324856832,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**2. Metrics List:**
```bash
curl http://localhost:8081/actuator/metrics
```

**Response:**
```json
{
  "names": [
    "http.server.requests",
    "jvm.memory.used",
    "jvm.memory.max",
    "system.cpu.usage",
    "process.uptime",
    "logback.events"
  ]
}
```

**3. HTTP Request Stats:**
```bash
curl http://localhost:8081/actuator/metrics/http.server.requests
```

**Response:**
```json
{
  "name": "http.server.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 25
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 1.234567
    },
    {
      "statistic": "MAX",
      "value": 0.123
    }
  ],
  "availableTags": [
    {
      "tag": "uri",
      "values": ["/api/anggota", "/api/anggota/{id}"]
    },
    {
      "tag": "status",
      "values": ["200", "404", "500"]
    }
  ]
}
```

### 🧪 Testing Actuator

**Test semua service:**
```bash
# Anggota
curl http://localhost:8081/actuator/health

# Buku
curl http://localhost:8082/actuator/health

# Peminjaman
curl http://localhost:8083/actuator/health

# Pengembalian
curl http://localhost:8084/actuator/health
```

**Monitor HTTP requests:**
```bash
# Make some requests
curl http://localhost:8081/api/anggota
curl http://localhost:8081/api/anggota/1

# Check stats
curl http://localhost:8081/actuator/metrics/http.server.requests
```

### ✅ Keuntungan

- **Health Monitoring:** Cek apakah service running dengan baik
- **Performance Metrics:** Monitor CPU, memory, request latency
- **Troubleshooting:** Identifikasi masalah dengan cepat
- **No Extra Setup:** Built-in Spring Boot, tinggal konfigurasi

---

## 6. Jenkins CI/CD

### 📌 Pengertian

Jenkins adalah automation server untuk Continuous Integration/Continuous Deployment (CI/CD).

### 🎯 Tujuan Implementasi

- Automated build saat ada code changes
- Automated testing untuk quality assurance
- Fail-fast mechanism jika ada test yang gagal

### 📂 Lokasi Implementasi

**File:** `Jenkinsfile` di root repository

### 💻 Jenkinsfile Explanation

```groovy
pipeline {
    agent any    // Jalankan di node mana saja
    
    tools {
        maven 'Maven'    // Maven configuration
        jdk 'JDK17'      // JDK configuration
    }
    
    stages {
        // Stage 1: Clone repository
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        // Stage 2: Build Anggota Service
        stage('Build Anggota Service') {
            steps {
                dir('anggota') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }
        
        // Stage 3: Test Anggota Service
        stage('Test Anggota Service') {
            steps {
                dir('anggota') {
                    bat 'mvn test'    // Jika fail → STOP
                }
            }
        }
        
        // ... repeat untuk buku, peminjaman, pengembalian
    }
    
    post {
        success {
            echo '✅ All services built successfully!'
        }
        failure {
            echo '❌ Build failed!'
        }
    }
}
```

### 🔧 Setup Jenkins

**1. Start Jenkins:**
```bash
java -jar jenkins.war --httpPort=8080
```

**2. Unlock Jenkins:**
- Ambil password dari: `C:\Users\user\.jenkins\secrets\initialAdminPassword`
- Paste di form unlock

**3. Install Plugins:**
- Install suggested plugins
- Tambahan: Git, Maven Integration, Pipeline

**4. Configure Tools:**

**Manage Jenkins → Global Tool Configuration:**

**Maven:**
- Name: `Maven`
- Install automatically: ✅
- Version: Maven 3.9.x

**JDK:**
- Name: `JDK17`
- Install automatically: ✅
- Version: OpenJDK 17

**5. Create Pipeline Job:**

**New Item:**
- Name: `Perpustakaan-Microservices`
- Type: Pipeline
- OK

**Pipeline Configuration:**
- Definition: `Pipeline script from SCM`
- SCM: `Git`
- Repository URL: `https://github.com/FailHy/perpustakaan.git`
- Branch: `*/main`
- Script Path: `Jenkinsfile`

**6. Build:**
- Click "Build Now"
- Watch Console Output

### 📊 Pipeline Stages

```
1. Checkout
   └─ Clone repository dari GitHub
   
2. Build Anggota Service
   └─ mvn clean package -DskipTests
   
3. Test Anggota Service
   └─ mvn test
   └─ ❌ Jika fail → STOP
   
4. Build Buku Service
   └─ mvn clean package -DskipTests
   
5. Test Buku Service
   └─ mvn test
   └─ ❌ Jika fail → STOP
   
6. Build Peminjaman Service
   └─ mvn clean package -DskipTests
   
7. Test Peminjaman Service
   └─ mvn test
   └─ ❌ Jika fail → STOP
   
8. Build Pengembalian Service
   └─ mvn clean package -DskipTests
   
9. Test Pengembalian Service
   └─ mvn test
   └─ ❌ Jika fail → STOP
   
10. Post-Build
    ├─ Success: Semua OK
    └─ Failure: Ada yang fail
```

### 🧪 Testing Jenkins Pipeline

**1. Trigger Build:**
- Method 1: Click "Build Now" di Jenkins UI
- Method 2: Push code ke GitHub → auto build (jika setup webhook)

**2. Watch Build Progress:**
- Stage View → Lihat progress tiap stage
- Console Output → Lihat log detail

**3. Simulate Build Failure:**

Edit test file untuk fail:
```java
// AnggotaControllerTest.java
@Test
void testGetAllAnggota() {
    assertEquals(999, service.getAllAnggota().size());  // Will fail
}
```

Commit & push → Build akan fail di stage "Test Anggota Service"

### 🎨 Cara Presentasi

**1. Demo Setup:**
- Tunjukkan Jenkins dashboard
- Explain pipeline configuration
- Show Jenkinsfile di repository

**2. Demo Build Process:**
- Click "Build Now"
- Show real-time console output
- Explain setiap stage

**3. Demo Success:**
- Semua stage hijau
- Build artifacts (.jar files) created

**4. Demo Failure:**
- Simulate test failure
- Show stage yang fail (merah)
- Explain fail-fast mechanism

**5. Explain Benefits:**
- Automated quality check
- Early bug detection
- Consistent build process

### ✅ Keuntungan

- **Automation:** Build & test otomatis tanpa manual
- **Quality Assurance:** Test dijalankan setiap build
- **Fail-Fast:** Stop segera jika ada masalah
- **Feedback:** Developer tahu langsung jika ada error
- **Consistency:** Proses build sama untuk semua

---

## 📝 Summary

| Teknologi | Tujuan | Benefit |
|-----------|--------|---------|
| **CQRS** | Pisah read/write | Performance, scalability |
| **RabbitMQ** | Event messaging | Decoupling, async processing |
| **Structured Logging** | ELK-ready logs | Searchable, traceable |
| **Distributed Tracing** | Track cross-service | Debug, performance analysis |
| **Actuator** | Monitoring | Health check, metrics |
| **Jenkins CI** | Automated build/test | Quality, consistency |

**Total:** 6 Teknologi Modern ✅

---

**Tanggal:** 25 Desember 2024  
**Status:** Production Ready 🚀
