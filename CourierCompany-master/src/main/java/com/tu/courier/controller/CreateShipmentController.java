package com.tu.courier.controller;

import com.tu.courier.dao.OfficeDAO;
import com.tu.courier.dao.ShipmentDAO;
import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.*;
import com.tu.courier.service.NotificationService;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateShipmentController {

    // ===== ПОДАТЕЛ =====
    @FXML private RadioButton rbSenderClient, rbSenderGuest;
    @FXML private HBox boxSenderSearch, boxSenderManual;
    @FXML private TextField tfSenderSearchPhone, tfSenderName, tfSenderPhone;
    @FXML private Label senderStatusLabel;

    // ===== ПОЛУЧАТЕЛ =====
    @FXML private RadioButton rbReceiverClient, rbReceiverGuest;
    @FXML private HBox boxReceiverSearch, boxReceiverManual;
    @FXML private TextField tfReceiverSearchPhone, tfReceiverName, tfReceiverPhone;
    @FXML private Label receiverStatusLabel;

    // ===== ДОСТАВКА =====
    @FXML private RadioButton rbToAddress, rbToOffice;
    @FXML private VBox boxAddress, boxOffice;
    @FXML private TextField tfAddress;
    @FXML private ComboBox<Office> cbOffice;

    // ===== ДРУГИ =====
    @FXML private TextField tfWeight;
    @FXML private ComboBox<ShipmentType> cbType;
    @FXML private Label priceLabel, statusLabel;

    private ToggleGroup senderGroup, receiverGroup, deliveryGroup;

    private User foundSenderUser;
    private User foundReceiverUser;

    private BigDecimal calculatedPrice = BigDecimal.ZERO;

    // ✅ Notifications
    private final NotificationService notificationService = new NotificationService();

    @FXML
    public void initialize() {
        senderGroup = new ToggleGroup();
        rbSenderClient.setToggleGroup(senderGroup);
        rbSenderGuest.setToggleGroup(senderGroup);

        receiverGroup = new ToggleGroup();
        rbReceiverClient.setToggleGroup(receiverGroup);
        rbReceiverGuest.setToggleGroup(receiverGroup);

        deliveryGroup = new ToggleGroup();
        rbToAddress.setToggleGroup(deliveryGroup);
        rbToOffice.setToggleGroup(deliveryGroup);

        setupToggleLogic();

        cbType.getItems().setAll(ShipmentType.values());
        cbType.setValue(ShipmentType.PACKAGE);

        loadOffices();
        clearStatus();
        setupClearOnChange();
        setupLivePriceUpdates();
        updatePriceUI();
    }

    private void setupLivePriceUpdates() {
        tfWeight.textProperty().addListener((obs, o, n) -> updatePriceUI());
        cbType.valueProperty().addListener((obs, o, n) -> updatePriceUI());
        deliveryGroup.selectedToggleProperty().addListener((obs, o, n) -> updatePriceUI());
    }

    private void setupClearOnChange() {
        senderGroup.selectedToggleProperty().addListener((obs, o, n) -> clearStatus());
        receiverGroup.selectedToggleProperty().addListener((obs, o, n) -> clearStatus());
        deliveryGroup.selectedToggleProperty().addListener((obs, o, n) -> clearStatus());

        tfSenderSearchPhone.textProperty().addListener((obs, o, n) -> clearStatus());
        tfSenderName.textProperty().addListener((obs, o, n) -> clearStatus());
        tfSenderPhone.textProperty().addListener((obs, o, n) -> clearStatus());

        tfReceiverSearchPhone.textProperty().addListener((obs, o, n) -> clearStatus());
        tfReceiverName.textProperty().addListener((obs, o, n) -> clearStatus());
        tfReceiverPhone.textProperty().addListener((obs, o, n) -> clearStatus());

        tfAddress.textProperty().addListener((obs, o, n) -> clearStatus());
        tfWeight.textProperty().addListener((obs, o, n) -> clearStatus());

        cbOffice.valueProperty().addListener((obs, o, n) -> clearStatus());
        cbType.valueProperty().addListener((obs, o, n) -> clearStatus());
    }

    private void setupToggleLogic() {

        senderGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean client = n == rbSenderClient;
            setVisible(boxSenderSearch, client);
            setVisible(boxSenderManual, !client);

            if (!client) {
                foundSenderUser = null;
                tfSenderSearchPhone.clear();
                tfSenderName.clear();
                tfSenderPhone.clear();
                senderStatusLabel.setText("Въведете данни за гост подател.");
            } else {
                tfSenderName.clear();
                tfSenderPhone.clear();
                senderStatusLabel.setText("");
            }
        });

        receiverGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean client = n == rbReceiverClient;
            setVisible(boxReceiverSearch, client);
            setVisible(boxReceiverManual, !client);

            if (!client) {
                foundReceiverUser = null;
                tfReceiverSearchPhone.clear();
                tfReceiverName.clear();
                tfReceiverPhone.clear();
                receiverStatusLabel.setText("Въведете данни за гост получател.");
            } else {
                tfReceiverName.clear();
                tfReceiverPhone.clear();
                receiverStatusLabel.setText("");
            }
        });

        deliveryGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            setVisible(boxAddress, n == rbToAddress);
            setVisible(boxOffice, n == rbToOffice);
        });

        setVisible(boxSenderSearch, true);
        setVisible(boxSenderManual, false);
        setVisible(boxReceiverSearch, true);
        setVisible(boxReceiverManual, false);
        setVisible(boxAddress, true);
        setVisible(boxOffice, false);
    }

    private void setVisible(javafx.scene.layout.Region r, boolean v) {
        r.setVisible(v);
        r.setManaged(v);
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        statusLabel.setStyle("");
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        statusLabel.setStyle("-fx-background-color: #fdecea; -fx-text-fill: #b00020; -fx-padding: 10; -fx-background-radius: 6;");
    }

    private void showSuccess(String msg) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        statusLabel.setStyle("-fx-background-color: #eaf7ee; -fx-text-fill: #1b5e20; -fx-padding: 10; -fx-background-radius: 6;");
    }

    private void loadOffices() {
        OfficeDAO dao = new OfficeDAO(HibernateUtil.getSessionFactory());
        cbOffice.getItems().setAll(dao.getAllOffices());

        cbOffice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Office office) {
                if (office == null) return "";
                return office.getCity() + " - " + office.getName();
            }

            @Override
            public Office fromString(String string) {
                return null;
            }
        });

        cbOffice.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Office office, boolean empty) {
                super.updateItem(office, empty);
                if (empty || office == null) {
                    setText(null);
                } else {
                    setText(office.getCity() + " - " + office.getName());
                }
            }
        });

        cbOffice.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Office office, boolean empty) {
                super.updateItem(office, empty);
                if (empty || office == null) {
                    setText("-- Избери --");
                } else {
                    setText(office.getCity() + " - " + office.getName());
                }
            }
        });
    }

    @FXML
    public void onSearchSender() {
        tfSenderSearchPhone.setText(tfSenderSearchPhone.getText().strip()); // UX: маха интервали веднага
        foundSenderUser = findUser(
                tfSenderSearchPhone.getText(),
                senderStatusLabel,
                tfSenderName,
                tfSenderPhone
        );
    }

    @FXML
    public void onSearchReceiver() {
        tfReceiverSearchPhone.setText(tfReceiverSearchPhone.getText().strip()); // UX: маха интервали веднага
        foundReceiverUser = findUser(
                tfReceiverSearchPhone.getText(),
                receiverStatusLabel,
                tfReceiverName,
                tfReceiverPhone
        );
    }

    private User findUser(String phone, Label label, TextField name, TextField phoneField) {
        String cleanPhone = (phone == null) ? "" : phone.strip();
        if (cleanPhone.isBlank()) {
            label.setText("Въведете телефон.");
            name.clear();
            phoneField.clear();
            return null;
        }

        User user = new UserDAO(HibernateUtil.getSessionFactory()).findByPhone(cleanPhone); // ✅ FIX
        if (user == null) {
            label.setText("Няма клиент с този телефон.");
            name.clear();
            phoneField.clear();
            return null;
        }

        name.setText(user.getFullName());
        phoneField.setText(user.getPhone());
        label.setText("Клиентът е намерен.");
        return user;
    }

    @FXML
    public void onSubmit() {

        Double w = parseWeightOrNull();
        if (w == null) {
            showError("Въведете валидно тегло (положително число).");
            return;
        }

        if (rbSenderClient.isSelected() && foundSenderUser == null) {
            showError("Намерете подател.");
            return;
        }

        if (rbReceiverClient.isSelected() && foundReceiverUser == null) {
            showError("Намерете получател.");
            return;
        }

        if (rbSenderGuest.isSelected()) {
            if (tfSenderName.getText().isBlank() || tfSenderPhone.getText().isBlank()) {
                showError("Попълнете гост подател (име и телефон).");
                return;
            }
        }

        if (rbReceiverGuest.isSelected()) {
            if (tfReceiverName.getText().isBlank() || tfReceiverPhone.getText().isBlank()) {
                showError("Попълнете гост получател (име и телефон).");
                return;
            }
        }

        Shipment s = new Shipment();

        if (rbSenderClient.isSelected()) {
            s.setSender(foundSenderUser);
            s.setSenderName(foundSenderUser.getFullName());
            s.setSenderPhone(foundSenderUser.getPhone());
        } else {
            s.setSender(null);
            s.setSenderName(tfSenderName.getText().strip());
            s.setSenderPhone(tfSenderPhone.getText().strip());
        }

        if (rbReceiverClient.isSelected()) {
            // ✅ FIX: липсваше setReceiver(...)
            s.setReceiver(foundReceiverUser);
            s.setReceiverName(foundReceiverUser.getFullName());
            s.setReceiverPhone(foundReceiverUser.getPhone());
        } else {
            s.setReceiver(null);
            s.setReceiverName(tfReceiverName.getText().strip());
            s.setReceiverPhone(tfReceiverPhone.getText().strip());
        }

        if (rbToOffice.isSelected()) {
            if (cbOffice.getValue() == null) {
                showError("Изберете офис за доставка.");
                return;
            }
            s.setToOffice(cbOffice.getValue());
        } else {
            String address = tfAddress.getText().strip();
            if (address.isBlank()) {
                showError("Въведете адрес за доставка.");
                return;
            }
            s.setDeliveryAddress(address);
        }

        s.setWeight(w);
        s.setPrice(calculatedPrice);
        s.setShipmentType(cbType.getValue());
        s.setStatus(ShipmentStatus.CREATED);
        s.setShipmentDate(LocalDateTime.now());
        s.setTrackingId(generateTrackingId());

        if (calculatedPrice == null || calculatedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Неуспешно изчисляване на цена. Проверете теглото и типа пратка.");
            return;
        }

        // ✅ Persist shipment
        new ShipmentDAO(HibernateUtil.getSessionFactory()).createShipment(s);

        // ✅ Notifications (след успешен запис)
        createShipmentNotifications(s);

        showSuccess("Пратката е създадена успешно!");

        tfWeight.clear();
        tfAddress.clear();
        cbOffice.setValue(null);
        tfReceiverSearchPhone.clear();
        tfReceiverName.clear();
        tfReceiverPhone.clear();
        receiverStatusLabel.setText("");
        updatePriceUI();
    }

    private void createShipmentNotifications(Shipment s) {
        // Пази 1 общ формат, за да е ясно и в UI-то
        String tracking = s.getTrackingId();
        String msgSender = "You created a shipment. Tracking ID: " + tracking;
        String msgReceiver = "A shipment was created for you. Tracking ID: " + tracking;
        String msgGlobal = "Shipment created. Tracking ID: " + tracking;

        // Sender/Receiver са null при guest
        if (s.getSender() != null) {
            notificationService.notifyUser(s.getSender(), s, msgSender);
        }
        if (s.getReceiver() != null) {
            notificationService.notifyUser(s.getReceiver(), s, msgReceiver);
        }

        // “Audit log” / общи известия (по желание, но ти го искаш за всяко действие)
        notificationService.notifyGlobal(s, msgGlobal);
    }

    public void setup(User user) {
        if (user != null && user.getRole() == Role.CLIENT) {
            rbSenderClient.setSelected(true);
            rbSenderGuest.setDisable(true);

            foundSenderUser = user;

            tfSenderName.setText(user.getFullName());
            tfSenderPhone.setText(user.getPhone());

            senderStatusLabel.setText("Подател: " + user.getFullName() + " (" + user.getPhone() + ")");

            setVisible(boxSenderSearch, false);
            setVisible(boxSenderManual, false);
        }
    }

    public void setupReceiver(User user) {
        if (user != null) {
            rbReceiverClient.setSelected(true);
            rbReceiverGuest.setSelected(false);

            foundReceiverUser = user;

            tfReceiverName.setText(user.getFullName());
            tfReceiverPhone.setText(user.getPhone());
            tfReceiverSearchPhone.setText(user.getPhone());

            receiverStatusLabel.setText("Получател: " + user.getFullName() + " (" + user.getPhone() + ")");

            setVisible(boxReceiverSearch, true);
            setVisible(boxReceiverManual, false);
        }
    }

    private BigDecimal calculatePrice(double weight, ShipmentType type, boolean toAddress) {
        BigDecimal price;

        switch (type) {
            case DOCUMENTS -> price = BigDecimal.valueOf(4.00).add(BigDecimal.valueOf(weight).multiply(BigDecimal.valueOf(1.20)));
            case PACKAGE -> price = BigDecimal.valueOf(6.00).add(BigDecimal.valueOf(weight).multiply(BigDecimal.valueOf(2.00)));
            case PALLET -> {
                price = BigDecimal.valueOf(12.00).add(BigDecimal.valueOf(weight).multiply(BigDecimal.valueOf(3.50)));
                BigDecimal min = BigDecimal.valueOf(25.00);
                if (price.compareTo(min) < 0) price = min;
            }
            default -> price = BigDecimal.ZERO;
        }

        if (toAddress) {
            price = price.add(BigDecimal.valueOf(4.00));
        }

        return price;
    }

    private Double parseWeightOrNull() {
        String txt = tfWeight.getText();
        if (txt == null) return null;

        txt = txt.strip().replace(',', '.');
        if (txt.isBlank()) return null;

        try {
            double w = Double.parseDouble(txt);
            if (w <= 0) return null;
            return w;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updatePriceUI() {
        Double w = parseWeightOrNull();
        ShipmentType type = cbType.getValue();
        boolean toAddress = rbToAddress.isSelected();

        if (w == null || type == null) {
            calculatedPrice = BigDecimal.ZERO;
            priceLabel.setText("0.00 лв.");
            return;
        }

        calculatedPrice = calculatePrice(w, type, toAddress);
        priceLabel.setText(String.format("%.2f лв.", calculatedPrice.doubleValue()));
    }

    private String generateTrackingId() {
        return java.util.UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
