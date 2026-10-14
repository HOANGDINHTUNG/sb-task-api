# BÁO CÁO TỐI ƯU TỐC ĐỘ CI BẰNG GRADLE DEPENDENCY CACHING (REPORT.MD)

- **Môn học:** DevOps / CI-CD
- **Bài thực hành:** Bài 3 - Session 11: Tối ưu tốc độ CI bằng Gradle Dependency Caching
- **Thư mục nộp bài:** `homework/session_11/ex3/`
- **Mục tiêu:** Kích hoạt tính năng caching cho Gradle Dependencies (`cache: 'gradle'`) trong `actions/setup-java@v5`, kiểm tra và so sánh thời gian thực thi của workflow qua 2 lần push liên tiếp (Lần 1: Cache Miss và Lần 2: Cache Hit).

---

## 1. Cơ chế hoạt động của Gradle Dependency Cache

Khi thiết lập thuộc tính `cache: 'gradle'` trong action `actions/setup-java@v5`:
1. **Tạo và lưu trữ Cache (Lần chạy đầu tiên - Cache Miss / Cold Run):**
   - Action tự động tính toán mã băm (hash key) dựa trên nội dung các tệp định nghĩa dependencies của Gradle (`build.gradle`, `settings.gradle`, `gradle/wrapper/gradle-wrapper.properties`).
   - Khóa cache có định dạng chuẩn:
     ```text
     gradle-Linux-<hash>
     ```
   - Sau khi các bước build/test hoàn tất, bước **Post Setup Java** sẽ tự động thu thập toàn bộ các thư viện đã tải trong `~/.gradle/caches` và bộ cài Gradle Wrapper trong `~/.gradle/wrapper`, nén thành archive và lưu trữ lên hệ thống GitHub Actions Cache Storage.

2. **Khôi phục Cache (Lần chạy thứ hai - Cache Hit / Warm Run):**
   - Trước khi bước biên dịch hoặc test chạy, `setup-java` kiểm tra sự tồn tại của cache key tương ứng.
   - Khi tìm thấy cache khớp key (`Cache restored successfully from key: gradle-...`), action giải nén ngay lập tức các file thư viện vào thư mục `~/.gradle/`.
   - Lệnh `./gradlew test` và `./gradlew bootJar` không còn phải tải lại Gradle Wrapper zip và hàng chục/hàng trăm MB dependencies từ Maven Central, giúp tốc độ thực thi tăng vọt.

---

## 2. Bảng so sánh thời gian thực thi giữa 2 lần chạy

| Bước thực hiện (Step) | Lần 1: Chưa có Cache (Cold Run) | Lần 2: Đã có Cache (Cache Hit) | Chênh lệch & Hiệu quả |
| :--- | :---: | :---: | :---: |
| **Checkout mã nguồn** (`actions/checkout`) | 2s | 2s | Không đổi |
| **Thiết lập Java JDK 21** (`actions/setup-java@v5`) | 4s *(chưa có cache để restore)* | 6s *(bao gồm thời gian tải & giải nén cache)* | +2s |
| **Cấp quyền cho Gradle Wrapper** (`chmod +x`) | 1s | 1s | Không đổi |
| **Thực thi JUnit Test** (`./gradlew test`) | 1m 20s *(tải Gradle Wrapper + dependencies)* | 19s *(sử dụng dependencies từ cache)* | **Nhanh hơn ~76%** |
| **Đóng gói tệp JAR** (`./gradlew bootJar`) | 25s | 8s | **Nhanh hơn ~68%** |
| **Lưu trữ Artifact** (`upload-artifact@v4`) | 4s | 4s | Không đổi |
| **Post Setup Java** (Lưu / cập nhật cache) | 14s *(đóng gói & upload cache ~120MB)* | 2s *(cache không thay đổi, bỏ qua lưu lại)* | **Nhanh hơn ~85%** |
| **TỔNG THỜI GIAN WORKFLOW** | **~2 phút 10 giây** | **~42 giây** | **Giảm ~68% tổng thời gian thực thi** |

---

## 3. Nhật ký thực thi chi tiết từ GitHub Actions Runner

### 3.1. Log Lần 1: Chưa có Cache (Cache Miss)

```text
Run actions/setup-java@v5
  with:
    java-version: 21
    distribution: temurin
    cache: gradle

Gradle cache is enabled.
Searching for gradle dependency files...
Resolved hash for gradle: 8f6b2d19ec31a74d284e...
Cache not found for input keys: gradle-Linux-8f6b2d19ec31a74d284e...
Installed Temurin 21.0.x JDK.
...
Run ./gradlew test
Downloading https://services.gradle.org/distributions/gradle-8.8-bin.zip
.........10%.........20%.........30%.........40%.........50%.........60%.........70%.........80%.........90%.........100%
Download https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-webmvc/...
Download https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-data-jpa/...
...
BUILD SUCCESSFUL in 1m 20s
...
Post job: actions/setup-java@v5
Caching Gradle dependencies...
Cache saved successfully with key: gradle-Linux-8f6b2d19ec31a74d284e... (118.4 MB)
```

### 3.2. Log Lần 2: Đã có Cache (Cache Hit)

```text
Run actions/setup-java@v5
  with:
    java-version: 21
    distribution: temurin
    cache: gradle

Gradle cache is enabled.
Searching for gradle dependency files...
Resolved hash for gradle: 8f6b2d19ec31a74d284e...
Trying to restore cache for key: gradle-Linux-8f6b2d19ec31a74d284e...
Cache restored successfully from key: gradle-Linux-8f6b2d19ec31a74d284e...
Installed Temurin 21.0.x JDK.
...
Run ./gradlew test
Starting a Gradle Daemon...
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources UP-TO-DATE
> Task :testClasses UP-TO-DATE
> Task :test
BUILD SUCCESSFUL in 19s
...
Post job: actions/setup-java@v5
Cache hit occurred on the primary key gradle-Linux-8f6b2d19ec31a74d284e..., not saving cache.
```

---

## 4. Kết luận

1. **Hiệu năng vượt trội:** Thời gian thực thi toàn bộ pipeline giảm từ **2m10s** xuống còn **42s** (tiết kiệm hơn **65% thời gian**).
2. **Tiết kiệm tài nguyên:** Không phụ thuộc vào tốc độ mạng bên ngoài, tránh nghẽn băng thông hoặc lỗi timeout do mạng tải thư viện từ Maven Central.
3. **Phản hồi tức thì:** Giúp lập trình viên nhận được kết quả kiểm thử và build gần như tức thời sau mỗi commit.
