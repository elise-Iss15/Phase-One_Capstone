module com.igirepay {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires com.almasb.fxgl.all;
    requires org.postgresql.jdbc;

    opens com.igirepay to javafx.fxml;
    opens com.igirepay.controller to javafx.fxml;

    exports com.igirepay;
    exports com.igirepay.model;
    exports com.igirepay.service;
    exports com.igirepay.console;
}
