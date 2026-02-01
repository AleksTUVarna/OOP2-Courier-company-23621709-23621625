package com.tu.courier.controller;

import com.tu.courier.dao.NotificationDAO;
import com.tu.courier.entity.Notification;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import com.tu.courier.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import com.tu.courier.entity.Role;


public class NotificationsController {

    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, LocalDateTime> colTime;
    @FXML private TableColumn<Notification, String> colMessage;
    @FXML private TableColumn<Notification, Boolean> colRead;
    @FXML private Label statusLabel;

    private final ObservableList<Notification> data = FXCollections.observableArrayList();
    private NotificationDAO notificationDAO;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy 'г.' HH:mm", new Locale("bg", "BG"));

    @FXML
    public void initialize() {
        notificationDAO = new NotificationDAO(HibernateUtil.getSessionFactory());

        colTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colRead.setCellValueFactory(new PropertyValueFactory<>("read"));

        colTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.format(timeFormatter));
            }
        });

        colRead.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : (item ? "Да" : "Не"));
            }
        });

        notificationsTable.setItems(data);
        reload();
    }

    @FXML
    private void onRefreshClick() {
        reload();
    }

    @FXML
    private void onMarkAllReadClick() {
        User current = SessionManager.getCurrentUser();
        if (current == null || current.getId() == null) {
            statusLabel.setText("Няма активен потребител.");
            return;
        }

        int changed = 0;
        for (Notification n : data) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationDAO.update(n);
                changed++;
            }
        }

        notificationsTable.refresh();
        statusLabel.setText("Маркирани като прочетени: " + changed);
    }

    private void reload() {
        User current = SessionManager.getCurrentUser();
        if (current == null || current.getId() == null) {
            statusLabel.setText("Няма активен потребител.");
            data.clear();
            return;
        }

        try {
            List<Notification> list;

            // ✅ ADMIN вижда всички
            if (current.getRole() == Role.ADMIN) {

                list = notificationDAO.findAll();
            } else {
                // ✅ CLIENT/COURIER виждат само своите
                list = notificationDAO.findForUser(current.getId());

                // Ако НЯКОЙ ДЕН решиш да има и глобални:
                // list = notificationDAO.findForUserIncludingGlobal(current.getId());
            }

            data.setAll(list);
            statusLabel.setText("Заредени известия: " + data.size());

        } catch (Exception e) {
            data.clear();
            statusLabel.setText("Грешка при зареждане на известия.");
            e.printStackTrace();
        }
    }
}
