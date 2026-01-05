package com.tu.courier.controller;

import com.tu.courier.dao.ShipmentDAO;
import com.tu.courier.entity.Shipment;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

public class MyShipmentsController {

    @FXML private TableView<Shipment> myShipmentsTable;
    @FXML private TableColumn<Shipment, String> colTrackingId;
    @FXML private TableColumn<Shipment, String> colType;
    @FXML private TableColumn<Shipment, String> colOtherParty;
    @FXML private TableColumn<Shipment, String> colAddress;
    @FXML private TableColumn<Shipment, Double> colPrice;
    @FXML private TableColumn<Shipment, String> colStatus;
    @FXML private TableColumn<Shipment, String> colDate;

    private User currentUser;

    public void setup(User user) {
        this.currentUser = user;
        loadData();
    }

    @FXML
    public void initialize() {
        colTrackingId.setCellValueFactory(new PropertyValueFactory<>("trackingId"));
        colAddress.setCellValueFactory(cell -> {
            Shipment s = cell.getValue();

            if (s.getToOffice() != null) {
                var o = s.getToOffice();
                return new SimpleStringProperty("Офис: " + o.getCity() + " - " + o.getName());
                // или: + " (" + o.getAddress() + ")"
            }

            String addr = s.getDeliveryAddress();
            return new SimpleStringProperty(addr == null ? "" : addr);
        });

        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("shipmentDate"));

        // Логика за колона "Роля" (Изпратил / Получил)
        colType.setCellValueFactory(cell -> {
            Shipment s = cell.getValue();
            if (s.getSender() != null && s.getSender().getId().equals(currentUser.getId())) {
                return new SimpleStringProperty("➡ Изпратил");
            } else {
                return new SimpleStringProperty("⬅ Получил");
            }
        });

        // Логика за "Кореспондент" (На кого пращам / От кого получавам)
        colOtherParty.setCellValueFactory(cell -> {
            Shipment s = cell.getValue();
            boolean isSender = (s.getSender() != null && s.getSender().getId().equals(currentUser.getId()));

            if (isSender) {
                // Аз съм подател -> покажи получателя
                return new SimpleStringProperty(s.getReceiverName() != null ? s.getReceiverName() : "Анонимен");
            } else {
                // Аз съм получател -> покажи подателя
                return new SimpleStringProperty(s.getSenderName() != null ? s.getSenderName() : "Анонимен");
            }
        });
    }

    @FXML
    public void onRefresh() {
        if (currentUser != null) loadData();
    }

    private void loadData() {
        ShipmentDAO dao = new ShipmentDAO(HibernateUtil.getSessionFactory());
        myShipmentsTable.setItems(FXCollections.observableArrayList(dao.getShipmentsByUser(currentUser)));
    }
}