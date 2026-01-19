# Microservices Perpustakaan

Sistem **Event-Driven Microservices** yang tangguh untuk mengelola operasional perpustakaan (Anggota, Buku, Peminjaman, Pengembalian). Proyek ini mendemonstrasikan pola arsitektur Java modern termasuk **CQRS**, **Pelacakan Terdistribusi**, **Pencatatan Terpusat (ELK)**, dan **Pemantauan (Prometheus/Grafana)**.

---

## Desain Arsitektur

Sistem ini menggunakan arsitektur terpisah di mana layanan berkomunikasi secara sinkron melalui HTTP (REST) ​​untuk pembacaan/validasi dan secara asinkron melalui **RabbitMQ** untuk perubahan status dan konsistensi bertahap.

### Pola Utama:

- **Layanan Mikro**: Layanan otonom untuk setiap domain (Anggota, Buku, Peminjaman, Pengembalian).

- **API Gateway**: Titik masuk tunggal menggunakan Spring Cloud Gateway.

- **Penemuan Layanan**: Netflix Eureka untuk pendaftaran layanan dinamis.

- **Berbasis Peristiwa**: Menggunakan RabbitMQ untuk memisahkan peristiwa "Peminjaman" dan "Pengembalian".

- **CQRS**: Pemisahan model Perintah (Penulisan) dan Kueri (Pembacaan) (diimplementasikan dalam `peminjaman-service`).

---

## Teknologi yang Digunakan

| Kategori               | Teknologi                       | Keterangan                                                      |
| :--------------------- | :------------------------------ | :-------------------------------------------------------------- |
| **Bahasa & Framework** | Java 21, Spring Boot 3.3.x      | Basis pengembangan aplikasi backend.                            |
| **Service Discovery**  | Spring Cloud Eureka (Netflix)   | Pendaftaran dan penemuan layanan dinamis.                       |
| **API Gateway**        | Spring Cloud Gateway            | Routing trafik dan titik masuk tunggal.                         |
| **Message Broker**     | RabbitMQ                        | Komunikasi asinkron antar layanan.                              |
| **Database**           | H2 Database                     | Database in-memory (disimpan ke file) untuk pengembangan cepat. |
| **Logging**            | Logstash, Elasticsearch, Kibana | Pengumpulan, penyimpanan, dan visualisasi log aplikasi.         |
| **Monitoring**         | Prometheus, Grafana             | Pengumpulan metrik performa dan dashboard visualisasi.          |
| **Tracing**            | Micrometer, Zipkin              | Melacak perjalanan request dari satu layanan ke layanan lain.   |
| **Containerization**   | Docker, Docker Compose          | Pengelolaan lingkungan aplikasi yang konsisten.                 |
| **CI/CD**              | Jenkins                         | Otomatisasi proses build dan test.                              |

---

## Daftar Layanan dan Port

Berikut adalah peta port untuk mengakses setiap layanan, baik dari dalam container maupun dari host laptop Anda.

| Nama Layanan        | Port Container (Internal) | Port Host (Akses Luar) | Fungsi Utama                                |
| :------------------ | :------------------------ | :--------------------- | :------------------------------------------ |
| **Eureka Server**   | 8761                      | `8761`                 | Registry untuk Service Discovery.           |
| **API Gateway**     | 9000                      | `9090`                 | Pintu masuk utama untuk semua request API.  |
| **Anggota Service** | 8081                      | `8081`                 | Manajemen data anggota perpustakaan.        |
| **Buku Service**    | 8082                      | `8082`                 | Manajemen data buku dan stok.               |
| **Peminjaman**      | 8083                      | `8083`                 | Logika peminjaman & validasi aturan bisnis. |
| **Pengembalian**    | 8084                      | `8084`                 | Logika pengembalian & perhitungan denda.    |
| **RabbitMQ UI**     | 15672                     | `15672`                | Dashboard manajemen antrian pesan.          |
| **Kibana**          | 5601                      | `5601`                 | Dashboard untuk melihat Log aplikasi.       |
| **Grafana**         | 3000                      | `3000`                 | Dashboard monitoring visual.                |
| **Prometheus**      | 9090                      | `9091`                 | Server pengumpul metrik sistem (Scraper).   |

---

## Panduan Instalasi dan Penggunaan

### Prasyarat

Pastikan komputer Anda telah terinstal:

1.  **Docker Desktop** (dengan WSL2 aktif pada Windows).
2.  **Java JDK 21** (jika ingin menjalankan build manual tanpa Docker).
3.  **Git** untuk kloning repositori.

### Langkah Menjalankan Aplikasi

1.  **Build Semua Layanan**
    Kami menyediakan skrip otomatis untuk melakukan kompilasi kode Java menjadi file JAR.
    Jalankan file `build_all.bat` dengan cara double-click atau melalui terminal:

    ```bash
    ./build_all.bat
    ```

2.  **Jalankan Infrastruktur Docker**
    Setelah proses build selesai, jalankan semua layanan menggunakan Docker Compose:

    ```bash
    docker-compose up -d --build
    ```

    Perintah ini akan membuat network virtual, menjalankan database, broker pesan, monitoring stack, dan kelima microservice secara bersamaan.

3.  **Verifikasi Kesehatan Sistem**
    Buka `http://localhost:8761` (Eureka Dashboard). Pastikan layanan-layanan berikut terdaftar dan berstatus UP:
    - ANGGOTA-SERVICE
    - BUKU-SERVICE
    - PEMINJAMAN-SERVICE
    - PENGEMBALIAN-SERVICE
    - API-GATEWAY

---

## Panduan Pemantauan (Observability)

### 1. Memantau Log (Kibana)

Digunakan untuk debugging dan melihat jejak error (stacktrace) dari semua aplikasi di satu tempat.

- **Akses**: [http://localhost:5601](http://localhost:5601)
- **Cara Pakai**: Masuk ke menu "Discover". Anda bisa memfilter log berdasarkan nama layanan (field `service`) atau trace ID untuk melihat alur request yang spesifik.

### 2. Memantau Performa (Grafana)

Digunakan untuk melihat kesehatan server seperti penggunaan RAM (Heap Memory), CPU, dan jumlah Thread aktif.

- **Akses**: [http://localhost:3000](http://localhost:3000)
- **Login Default**: admin / admin.
- **Dashboard**: Buka menu Dashboards dan pilih dashboard "JVM (Micrometer)" yang telah dikonfigurasi.

### 3. Memantau Antrian Pesan (RabbitMQ)

Digunakan untuk memastikan pesan antar layanan terkirim dan tidak ada antrian yang macet (bottleneck).

- **Akses**: [http://localhost:15672](http://localhost:15672)
- **Login Default**: admin / password.

---

## Referensi API

Semua request dari klien (Postman/Frontend) harus diarahkan melalui **API Gateway** pada port **9090**.

**Base URL**: `http://localhost:9090`

### Layanan Anggota

- **Daftar Anggota Baru**: `POST /api/anggota`
- **Lihat Semua Anggota**: `GET /api/anggota`

### Layanan Buku

- **Tambah Buku**: `POST /api/buku`
- **Lihat Stok Buku**: `GET /api/buku`

### Layanan Peminjaman

- **Pinjam Buku**: `POST /api/peminjaman`
  - _Payload mencakup ID Anggota dan ID Buku._
  - _Sistem akan memvalidasi stok dan status anggota secara real-time._

### Layanan Pengembalian

- **Kembalikan Buku**: `POST /api/pengembalian`
  - _Payload mencakup ID Peminjaman._
  - _Sistem akan otomatis menghitung denda jika terlambat._

<- Add jenkins.war Jenkins Automation Test Trigger -->
