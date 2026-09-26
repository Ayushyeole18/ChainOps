package com.supplychainx.controller;

import com.supplychainx.model.Warehouse;
import com.supplychainx.service.ReportService;
import com.supplychainx.service.WarehouseService;
import com.supplychainx.ui.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class ReportController {

    @FXML private ComboBox<ReportService.ReportType> reportTypeCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> warehouseFilterCombo;
    @FXML private Label reportTitleLabel;
    @FXML private Label reportSummaryLabel;

    @FXML private TableView<List<String>> reportTable;

    private final ReportService reportService = new ReportService();
    private final WarehouseService warehouseService = new WarehouseService();
    private List<Warehouse> warehouses;

    @FXML
    public void initialize() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(ReportService.ReportType.values()));
        reportTypeCombo.setValue(ReportService.ReportType.INVENTORY);

        warehouses = warehouseService.getAllWarehouses();
        ObservableList<String> whNames = FXCollections.observableArrayList("ALL WAREHOUSES");
        for (Warehouse w : warehouses) {
            whNames.add(w.getWarehouseName());
        }
        warehouseFilterCombo.setItems(whNames);
        warehouseFilterCombo.setValue("ALL WAREHOUSES");

        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());

        handleGenerateReport();
    }

    @FXML
    private void handleGenerateReport() {
        ReportService.ReportType type = reportTypeCombo.getValue();
        if (type == null) return;

        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        String selectedWh = warehouseFilterCombo.getValue();
        Integer whId = null;
        if (selectedWh != null && !selectedWh.equalsIgnoreCase("ALL WAREHOUSES")) {
            for (Warehouse w : warehouses) {
                if (w.getWarehouseName().equalsIgnoreCase(selectedWh)) {
                    whId = w.getWarehouseId();
                    break;
                }
            }
        }

        try {
            ReportService.ReportResult result = reportService.generateReport(type, from, to, whId);
            reportTitleLabel.setText(type.getTitle());
            reportSummaryLabel.setText("Total Records: " + result.getRows().size() + " | Generated: " + LocalDate.now());

            // Build dynamic table columns
            reportTable.getColumns().clear();
            for (int i = 0; i < result.getHeaders().size(); i++) {
                final int colIdx = i;
                TableColumn<List<String>, String> col = new TableColumn<>(result.getHeaders().get(i));
                col.setCellValueFactory(cellData -> {
                    List<String> row = cellData.getValue();
                    return new SimpleStringProperty(colIdx < row.size() ? row.get(colIdx) : "");
                });
                col.setPrefWidth(140);
                reportTable.getColumns().add(col);
            }

            reportTable.setItems(FXCollections.observableArrayList(result.getRows()));
        } catch (Exception e) {
            AlertUtil.showError("Report Error", e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        ReportService.ReportType type = reportTypeCombo.getValue();
        if (type == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report to CSV");
        fileChooser.setInitialFileName("ChainOps_" + type.name().toLowerCase() + "_report.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Comma Separated (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try {
                LocalDate from = fromDatePicker.getValue();
                LocalDate to = toDatePicker.getValue();
                Integer whId = null;
                String selectedWh = warehouseFilterCombo.getValue();
                if (selectedWh != null && !selectedWh.equalsIgnoreCase("ALL WAREHOUSES")) {
                    for (Warehouse w : warehouses) {
                        if (w.getWarehouseName().equalsIgnoreCase(selectedWh)) {
                            whId = w.getWarehouseId();
                            break;
                        }
                    }
                }
                reportService.exportReportToCsv(type, from, to, whId, file);
                AlertUtil.showSuccess("CSV Exported", "Report successfully exported to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                AlertUtil.showError("Export Failed", e.getMessage());
            }
        }
    }
}
