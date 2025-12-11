package dev.chytac.camera;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Push
@EnableAsync
@Theme("camera")
@SpringBootApplication
public class CameraApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(CameraApplication.class, args);
    }

    @Bean("cameraExecutor")
    public Executor cameraExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Camera-");
        executor.initialize();
        return executor;
    }
}
