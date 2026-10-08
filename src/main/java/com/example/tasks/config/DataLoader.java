package com.example.tasks.config;

import com.example.tasks.model.Task;
import com.example.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    private final TaskRepository taskRepository;

    @Autowired
    public DataLoader(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (taskRepository.count() == 0) {
            taskRepository.saveAll(Arrays.asList(
                    new Task("Học Spring Boot", "Tìm hiểu cơ bản về Spring Boot và tạo project đầu tiên", true),
                    new Task("Cài đặt Docker", "Cài đặt Docker Desktop và chạy thử container nginx", true),
                    new Task("Viết REST API", "Hoàn thành API /api/tasks lấy dữ liệu mock", true),
                    new Task("Kết nối Database", "Cấu hình JPA và MySQL cho project", false),
                    new Task("Tạo Repository", "Tạo interface TaskRepository kế thừa JpaRepository", false),
                    new Task("Seed Dữ liệu", "Sử dụng CommandLineRunner để thêm dữ liệu mẫu vào db", false),
                    new Task("Học Git cơ bản", "Các lệnh cơ bản: add, commit, push, pull", true),
                    new Task("Triển khai lên server", "Deploy ứng dụng Spring Boot lên VPS", false),
                    new Task("Cấu hình CI/CD", "Sử dụng GitHub Actions để tự động hóa build và deploy", false),
                    new Task("Viết Unit Test", "Sử dụng JUnit và Mockito để test các service", false)
            ));
            System.out.println("Đã thêm 10 dữ liệu mẫu vào cơ sở dữ liệu!");
        }
    }
}
