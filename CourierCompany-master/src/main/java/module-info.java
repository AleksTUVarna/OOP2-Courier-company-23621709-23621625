module com.tu.courier {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;

    requires java.sql;
    requires java.naming;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    requires org.apache.logging.log4j;
    requires static lombok;

    opens com.tu.courier.controller to javafx.fxml;
    opens com.tu.courier.entity to org.hibernate.orm.core, javafx.base;
    opens com.tu.courier.dto to javafx.base;

    exports com.tu.courier;
}
