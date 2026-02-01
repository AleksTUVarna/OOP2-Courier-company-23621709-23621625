package com.tu.courier.controller;

import com.tu.courier.dao.ReportDAO;
import com.tu.courier.dto.EnumCountRow;
import com.tu.courier.dto.OfficeReportRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportsController {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    // Summary labels
    @FXML private Label totalCountLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label avgPriceLabel;
    @FXML private Label avgWeightLabel;

    // By Office table
    @FXML private TableView<OfficeReportRow> byOfficeTable;
    @FXML private TableColumn<OfficeReportRow, String> officeNameCol;
    @FXML private TableColumn<OfficeReportRow, Long> shipmentsCountCol;
    @FXML private TableColumn<OfficeReportRow, Double> revenueCol;

    // By Status table
    @FXML private TableView<EnumCountRow> byStatusTable;
    @FXML private TableColumn<EnumCountRow, String> statusKeyCol;
    @FXML private TableColumn<EnumCountRow, Long> statusCountCol;

    // By Type table
    @FXML private TableView<EnumCountRow> byTypeTable;
    @FXML private TableColumn<EnumCountRow, String> typeKeyCol;
    @FXML private TableColumn<EnumCountRow, Long> typeCountCol;

    private final ReportDAO reportDAO = new ReportDAO();

    @FXML
    public void initialize() {
        // Default: wide range so you don't miss test data
        LocalDate today = LocalDate.now();
        fromDatePicker.setValue(today.minusDays(365));
        toDatePicker.setValue(today.plusDays(365));

        // Summary placeholders
        totalCountLabel.setText("Total shipments: -");
        totalRevenueLabel.setText("Total revenue: -");
        avgPriceLabel.setText("Avg price: -");
        avgWeightLabel.setText("Avg weight: -");

        // Table placeholders (UX)
        byOfficeTable.setPlaceholder(new Label("No data for selected period."));
        byStatusTable.setPlaceholder(new Label("No data for selected period."));
        byTypeTable.setPlaceholder(new Label("No data for selected period."));

        // Column bindings (must match getters in DTOs)
        officeNameCol.setCellValueFactory(new PropertyValueFactory<>("officeName"));
        shipmentsCountCol.setCellValueFactory(new PropertyValueFactory<>("shipmentsCount"));
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        statusKeyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
        statusCountCol.setCellValueFactory(new PropertyValueFactory<>("count"));

        typeKeyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
        typeCountCol.setCellValueFactory(new PropertyValueFactory<>("count"));

        byOfficeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        byStatusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        byTypeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

    @FXML
    public void onGenerate() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null || toDate == null) {
            showAlert("Invalid period", "Please select both From and To dates.");
            return;
        }
        if (fromDate.isAfter(toDate)) {
            showAlert("Invalid period", "From date must be before or equal to To date.");
            return;
        }

        // Inclusive range: [from 00:00:00, to 23:59:59.999999999]
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);

        // Debug range
        System.out.println("REPORT RANGE: " + from + " -> " + to);

        // Summary
        ReportDAO.Summary summary = reportDAO.getSummary(from, to);
        totalCountLabel.setText("Total shipments: " + summary.totalCount());
        totalRevenueLabel.setText("Total revenue: " + formatMoney(summary.totalRevenue()));
        avgPriceLabel.setText("Avg price: " + formatMoney(summary.avgPrice()));
        avgWeightLabel.setText("Avg weight: " + formatNumber(summary.avgWeight()));

        // Tables
        var officeRows = reportDAO.getByOffice(from, to);
        var statusRows = reportDAO.getCountByStatus(from, to);
        var typeRows = reportDAO.getCountByType(from, to);

        // Debug sizes
        System.out.println("OFFICE rows = " + officeRows.size());
        System.out.println("STATUS rows = " + statusRows.size());
        System.out.println("TYPE rows = " + typeRows.size());

        byOfficeTable.setItems(FXCollections.observableArrayList(officeRows));
        byStatusTable.setItems(FXCollections.observableArrayList(statusRows));
        byTypeTable.setItems(FXCollections.observableArrayList(typeRows));

        // Force refresh (понякога помага при динамично зареждане в StackPane)
        byOfficeTable.refresh();
        byStatusTable.refresh();
        byTypeTable.refresh();

        if (byOfficeTable.getItems().isEmpty()) {
            byOfficeTable.getItems().add(
                    new OfficeReportRow("TEST OFFICE", 1, 10.0)
            );
        }

    }

    private String formatMoney(Double value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }

    private String formatNumber(Double value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
