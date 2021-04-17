package com.faforever.gw;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@Slf4j
public class GwTestClientApplication extends Application {
    private ConfigurableApplicationContext applicationContext;

    public static void applicationMain(String[] args) {
//        SpringApplication.run(GwTestClientApplication.class, args);
        launch(args);
    }

    @Override
    public void init() throws Exception {
        SpringApplication app = new SpringApplication(GwTestClientApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        applicationContext = app.run();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Galactic War developer client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
