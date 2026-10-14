# Bài tập 2: Biên dịch đóng gói tệp ứng dụng .jar với Gradle và lưu trữ Artifacts

## 1. Mục tiêu
- Thực thi câu lệnh biên dịch và đóng gói ứng dụng Spring Boot thành tệp `.jar` bằng Gradle: `./gradlew bootJar`.
- Lưu trữ thành phẩm biên dịch (`.jar`) từ thư mục `build/libs/` lên GitHub Actions bằng action `actions/upload-artifact@v4` với tên `app-jar`.

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
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

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

## 3. Ảnh chụp màn hình kết quả chạy trên GitHub Actions
*(Sau khi push commit lên GitHub và workflow chạy xong, chụp ảnh tab Actions > Summary hiển thị Artifact `app-jar` và lưu vào thư mục này)*
