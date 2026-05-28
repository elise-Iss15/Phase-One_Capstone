package com.igirepay;

import com.igirepay.controller.LoginController;
import com.igirepay.service.PaymentService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class igirePayApplication extends Application {
    private final PaymentService service = new PaymentService();
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/igirepay/login-view.fxml"));
        Parent root = loader.load();

        LoginController loginController = loader.getController();
        loginController.setService(service);
        loginController.setPrimaryStage(stage);

        stage.setTitle("IgirePay - Secure Gateway System");
        stage.setScene(new Scene(root, 400, 320));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}