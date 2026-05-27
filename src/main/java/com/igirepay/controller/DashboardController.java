package com.igirepay.controller;

import com.igirepay.model.Account;
import com.igirepay.model.Transaction;
import com.igirepay.service.PaymentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    private PaymentService paymentService;
    private Account currentAccount;
    private Stage stage;

    // Direct interface injection mappings from dashboard-view.fxml
    @FXML private Label customerNameLabel;
    @FXML private Label accountInfoLabel;
    @FXML private Label balanceLabel;
    @FXML private TextField amountField;
    @FXML private TextField referenceField;
    @FXML private TextField toAccountField;
    @FXML private ListView<String> historyList;

    public void setup(PaymentService service, Account account, Stage stage) {
        this.paymentService = service;
        this.currentAccount = account;
        this.stage = stage;

        refreshDataViews();
    }

    private void refreshDataViews() {
        if (currentAccount == null) return;

        customerNameLabel.setText("Welcome Back, Client Account Context");
        accountInfoLabel.setText("Account ID Number: " + currentAccount.getId());

        try {
            // Pull balances directly out of active SQL configurations
            double liveBalance = paymentService.getBalance(currentAccount.getId());
            balanceLabel.setText("Current Balance: " + liveBalance + " RWF");

            // Populating visual transaction log tracking arrays
            historyList.getItems().clear();
            List<Transaction> records = paymentService.getTransactionHistory(currentAccount.getId());
            if (records != null && !records.isEmpty()) {
                for (Transaction tx : records) {
                    historyList.getItems().add(tx.getType() + ": " + tx.getAmount() + " RWF [Ref: " + tx.getReferenceId() + "]");
                }
            } else {
                historyList.getItems().add("No transaction log history recorded.");
            }
        } catch (SQLException e) {
            balanceLabel.setText("Database Connection Sync Failed.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeposit() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            String ref = referenceField.getText().trim();
            if (ref.isEmpty()) ref = "DEP-" + System.currentTimeMillis();

            paymentService.deposit(currentAccount.getId(), amount, ref);

            amountField.clear();
            referenceField.clear();
            refreshDataViews();
        } catch (Exception e) {
            balanceLabel.setText("Processing Error: " + e.getLocalizedMessage());
        }
    }

    @FXML
    private void handleWithdraw() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            String ref = referenceField.getText().trim();
            if (ref.isEmpty()) ref = "WTH-" + System.currentTimeMillis();

            paymentService.withdraw(currentAccount.getId(), amount, ref);

            amountField.clear();
            referenceField.clear();
            refreshDataViews();
        } catch (Exception e) {
            balanceLabel.setText("Processing Error: " + e.getLocalizedMessage());
        }
    }

    @FXML
    private void onTransferClick() {
        try {
            long targetAccount = Long.parseLong(toAccountField.getText().trim());
            double amount = Double.parseDouble(amountField.getText().trim());
            String ref = referenceField.getText().trim();
            if (ref.isEmpty()) ref = "TRF-" + System.currentTimeMillis();

            paymentService.transfer(currentAccount.getId(), targetAccount, amount, ref);

            amountField.clear();
            referenceField.clear();
            toAccountField.clear();
            refreshDataViews();
        } catch (Exception e) {
            balanceLabel.setText("Processing Error: " + e.getLocalizedMessage());
        }
    }

    @FXML
    private void onLogoutClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/igirepay/login-view.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setService(paymentService);
            loginController.setPrimaryStage(stage);

            stage.setTitle("IgirePay - Secure Access Authentication");
            stage.setScene(new Scene(root, 400, 320));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}