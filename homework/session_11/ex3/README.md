# Bài tập 3: Tối ưu tốc độ CI bằng Gradle Dependency Caching

## 1. Mục tiêu
- Kích hoạt tính năng Caching cho Gradle Dependencies bằng tham số `cache: 'gradle'` trong action `actions/setup-java@v5` tại tệp `.github/workflows/ci.yml`.
- Giảm thiểu thời gian tải các tệp thư viện từ Maven Central và bộ cài Gradle Wrapper mỗi khi luồng CI khởi chạy.
- So sánh thời gian thực thi giữa lần đầu (Cold Run / Cache Miss) và lần sau (Warm Run / Cache Hit).

## 2. Kịch bản GitHub Actions (`.github/workflows/ci.yml`)
```yaml
name: Java Spring Boot CI (Gradle)

on:
  push:
    branches: [ "master", "main" ]

jobs:
  test:
    name: Run Unit Tests
    runs-on: ubuntu-latest

    steps:
      - name: Checkout mã nguồn
        uses: actions/checkout@v4

      - name: Thiết lập môi trường Java JDK 21
        uses: actions/setup-java@v5
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Cấp quyền cho Gradle Wrapper
        run: chmod +x gradlew

      - name: Thực thi JUnit Test với Gradle
        run: ./gradlew test

      - name: Đóng gói tệp JAR với Gradle
        run: ./gradlew bootJar

      - name: Lưu trữ thành phẩm JAR Artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: build/libs/*.jar
```

## 3. Báo cáo đánh giá và so sánh thời gian thực thi
Chi tiết kết quả thực nghiệm, nhật ký Runner và bảng so sánh thời gian thực thi được lưu tại [report.md](report.md).
