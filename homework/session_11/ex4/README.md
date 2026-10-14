# Bài tập 4: Khai báo GitHub Secrets và cấu hình Đăng nhập Container Registry

## 1. Mục tiêu
- Tạo tệp mã hóa an toàn (**Repository Secret**) lưu trữ Token cá nhân trên GitHub.
- Cấu hình bước xác thực đăng nhập tự động vào **GitHub Container Registry (GHCR)** trong luồng CI.

---

## 2. Hướng dẫn từng bước thực hiện

### Bước 1: Khởi tạo Personal Access Token (PAT) trên GitHub
1. Truy cập vào GitHub: Nhấp vào **Avatar cá nhân** (góc trên bên phải) -> Chọn **Settings**.
2. Cuộn xuống menu bên trái, chọn **Developer Settings** -> **Personal access tokens** -> **Tokens (classic)**.
3. Chọn **Generate new token** -> **Generate new token (classic)**.
4. Điền các thông tin:
   - **Note:** `GHCR_REGISTRY_TOKEN` (hoặc tên gợi nhớ tuỳ ý).
   - **Expiration:** Chọn thời hạn (ví dụ: `30 days` hoặc `90 days`).
   - **Select scopes:** Tích chọn quyền:
     - `write:packages` (Hệ thống sẽ tự động tích kèm `read:packages`).
     - *(Tùy chọn khuyến nghị)*: Tích chọn `delete:packages` hoặc `repo` nếu cần quản lý trọn vẹn packages của repository cá nhân.
5. Cuộn xuống cuối trang và bấm **Generate token**.
6. **Sao chép (Copy) mã Token vừa tạo** (Lưu ý: Token chỉ hiển thị 1 lần duy nhất).

---

### Bước 2: Cấu hình Repository Secret trên GitHub Repository
1. Điều hướng tới repository của dự án: `HOANGDINHTUNG/sb-task-api`.
2. Chọn tab **Settings** của repository.
3. Ở menu bên trái, tìm đến mục **Security** -> chọn **Secrets and variables** -> **Actions**.
4. Nhấp vào nút **New repository secret**.
5. Nhập các trường thông tin:
   - **Name:** `REGISTRY_TOKEN`
   - **Secret:** Dán mã Token (PAT) đã sao chép ở Bước 1.
6. Nhấp **Add secret** để hoàn tất việc lưu trữ bí mật.

---

### Bước 3: Cấu hình bước Đăng nhập Registry trong `.github/workflows/ci.yml`

Khai báo bước đăng nhập sử dụng action chính thức `docker/login-action@v3`:

```yaml
      - name: Đăng nhập vào GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.REGISTRY_TOKEN }}
```

#### Toàn bộ nội dung kịch bản `ci.yml`:
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

      - name: Đăng nhập vào GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.REGISTRY_TOKEN }}
```

---

## 3. Kiểm tra và Xác nhận kết quả

1. Đẩy commit mới lên GitHub:
   ```bash
   git add .
   git commit -m "ci: add ghcr docker login action step with REGISTRY_TOKEN secret"
   git push origin master
   ```
2. Mở tab **Actions** trên repository GitHub và theo dõi workflow đang chạy.
3. Khi workflow hoàn tất thành công, mở chi tiết job `Run Unit Tests` và kiểm tra bước **Đăng nhập vào GitHub Container Registry**:
   - Log đầu ra hiển thị thông báo:
     ```text
     Run docker/login-action@v3
     with:
       registry: ghcr.io
       username: HOANGDINHTUNG
       password: ***
       logout: true
     Logging into ghcr.io...
     Login Succeeded!
     ```

---

## 4. Bằng chứng nộp bài (Evidence & Screenshots)

### 4.1. Ảnh chụp màn hình Repository Secrets (`REGISTRY_TOKEN`)
> Bạn chụp màn hình mục **Settings -> Secrets and variables -> Actions** hiển thị secret `REGISTRY_TOKEN` và lưu vào thư mục này với tên `registry_token_secret.png` hoặc chèn vào dưới:

*(Vị trí đính kèm ảnh chụp màn hình Secret `REGISTRY_TOKEN`)*
![Repository Secret REGISTRY_TOKEN](registry_token_secret.png)

### 4.2. Ảnh chụp màn hình Log Actions hiển thị `Login Succeeded!`
> Bạn chụp màn hình chi tiết step **Đăng nhập vào GitHub Container Registry** trên tab Actions hiển thị dòng chữ `Login Succeeded!` và lưu vào thư mục này với tên `login_succeeded.png`:

*(Vị trí đính kèm ảnh chụp màn hình Log Login Succeeded)*
![Login Succeeded Log](login_succeeded.png)
