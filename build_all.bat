@echo off
echo Starting local build for all services...

echo [1/5] Building Anggota Service...
cd anggota
call mvnw clean package -DskipTests
cd ..

echo [2/5] Building Buku Service...
cd buku
call mvnw clean package -DskipTests
cd ..

echo [3/5] Building Peminjaman Service...
cd peminjaman
call mvnw clean package -DskipTests
cd ..

echo [4/5] Building Pengembalian Service...
cd pengembalian
call mvnw clean package -DskipTests
cd ..

echo [5/5] Building API Gateway...
cd api-gateway
call mvnw clean package -DskipTests
cd ..

echo ===================================================
echo BUILD COMPLETE.
echo Now you can run: docker-compose up -d --build
echo ===================================================
pause
