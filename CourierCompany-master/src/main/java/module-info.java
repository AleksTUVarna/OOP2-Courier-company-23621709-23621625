module com.tu.courier {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.naming; // Нужен за Hibernate
    requires java.sql;    // Нужен за връзката с базата
    requires static lombok;

    // Отваряме пакетите за JavaFX и Hibernate
    opens com.tu.courier to javafx.fxml;
    exports com.tu.courier;

    opens com.tu.courier.controller to javafx.fxml;
    exports com.tu.courier.controller;

    opens com.tu.courier.entity to org.hibernate.orm.core;
    exports com.tu.courier.entity;
}