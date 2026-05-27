package com.igirepay.controller;

import com.igirepay.model.Account;
import com.igirepay.service.PaymentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField accountIdField;
    @FXML private PasswordField pinField;
    @FXML private Label messageLabel;

    private PaymentService service;
    private Stage primaryStage;

    public void setService(PaymentService service) {
        this.service = service;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void onLoginClick() {
        messageLabel.setText("");

        if (accountIdField.getText().isBlank() || pinField.getText().isBlank()) {
            messageLabel.setText("Please enter account number and PIN.");
            return;
        }

        try {
            long accountId = Long.parseLong(accountIdField.getText().trim());
            int pin = Integer.parseInt(pinField.getText().trim());


            boolean isValidPin = service.validatePin(accountId, pin);
            if (!isValidPin) {
                messageLabel.setText("Wrong account number or PIN.");
                return;
            }

            Account activeAccount = service.getCustomerAccounts(accountId).stream()
                    .filter(acc -> acc.getId() == accountId)
                    .findFirst()
                    .orElse(null);

            if (activeAccount == null) {
                activeAccount = new com.igirepay.model.WalletAccount();
                activeAccount.setId(accountId);
                activeAccount.setPin(pin);
            }

            openDashboard(activeAccount);
        } catch (NumberFormatException e) {
            messageLabel.setText("Account number and PIN must be numbers.");
        } catch (SQLException e) {
            messageLabel.setText("Database operational validation failure.");
            e.printStackTrace();
        } catch (IOException e) {
            messageLabel.setText("Could not open dashboard view template.");
            e.printStackTrace();
        }
    }

    private void openDashboard(Account account) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/igirepay/dashboard-view.fxml"));
        Parent root = loader.load();

        DashboardController dashboard = loader.getController();
        dashboard.setup(service, account, primaryStage);

        primaryStage.setTitle("IgirePay Core Gateway Dashboard");
        primaryStage.setScene(new Scene(root, 520, 560));
        primaryStage.centerOnScreen();
    }
}