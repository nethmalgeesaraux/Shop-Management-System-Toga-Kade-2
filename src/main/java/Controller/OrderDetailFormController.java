package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Models.Dto.OrderDetail;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class OrderDetailFormController implements Initializable {

    @FXML
    private TextField txtOrderID, txtItemCode, txtOrderQty, txtDiscount;
    @FXML
    private TableView<OrderDetail> tblOrderDetails;
    @FXML
    private TableColumn<OrderDetail, String> colOrderID, colItemCode, colDescription;
    @FXML
    private TableColumn<OrderDetail, Integer> colOrderQty;
    @FXML
    private TableColumn<OrderDetail, Double> colUnitPrice, colDiscount, colTotal;
    @FXML
    private Label lblTotalSales;

    private OrderDetailController orderDetailController;
    private ObservableList<OrderDetail> orderDetailList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderDetailController = new OrderDetailController();
        orderDetailList = FXCollections.observableArrayList();

        initializeTableColumns();
        tblOrderDetails.setItems(orderDetailList);
        loadAllOrderDetails();

        // Add table row selection listener
        tblOrderDetails.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        setOrderDetailData(newValue);
                    }
                });
    }

    private void initializeTableColumns() {
        colOrderID.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        colItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colOrderQty.setCellValueFactory(new PropertyValueFactory<>("orderQty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    @FXML
    private void saveOrderDetail() {
        try {
            if (validateFields()) {
                OrderDetail orderDetail = new OrderDetail(
                        txtOrderID.getText().toUpperCase(),
                        txtItemCode.getText().toUpperCase(),
                        Integer.parseInt(txtOrderQty.getText()),
                        Double.parseDouble(txtDiscount.getText())
                );

                if (orderDetailController.saveOrderDetail(orderDetail)) {
                    showAlert("Success", "Order detail saved successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllOrderDetails();
                } else {
                    showAlert("Error", "Failed to save order detail!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error saving order detail: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void updateOrderDetail() {
        try {
            if (validateFields()) {
                OrderDetail orderDetail = new OrderDetail(
                        txtOrderID.getText().toUpperCase(),
                        txtItemCode.getText().toUpperCase(),
                        Integer.parseInt(txtOrderQty.getText()),
                        Double.parseDouble(txtDiscount.getText())
                );

                if (orderDetailController.updateOrderDetail(orderDetail)) {
                    showAlert("Success", "Order detail updated successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllOrderDetails();
                } else {
                    showAlert("Error", "Failed to update order detail!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating order detail: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void deleteOrderDetail() {
        try {
            if (txtOrderID.getText().isEmpty() || txtItemCode.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter Order ID and Item Code to delete", Alert.AlertType.WARNING);
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setContentText("Are you sure you want to delete this order detail?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                if (orderDetailController.deleteOrderDetail(
                        txtOrderID.getText().toUpperCase(),
                        txtItemCode.getText().toUpperCase())) {

                    showAlert("Success", "Order detail deleted successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllOrderDetails();
                } else {
                    showAlert("Error", "Order detail not found!", Alert.AlertType.WARNING);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error deleting order detail: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchOrderDetail() {
        try {
            if (txtOrderID.getText().isEmpty() || txtItemCode.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter both Order ID and Item Code to search", Alert.AlertType.WARNING);
                return;
            }

            OrderDetail orderDetail = orderDetailController.searchOrderDetail(
                    txtOrderID.getText().toUpperCase(),
                    txtItemCode.getText().toUpperCase());

            if (orderDetail != null) {
                setOrderDetailData(orderDetail);
            } else {
                showAlert("Not Found", "Order detail not found!", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            showAlert("Error", "Error searching order detail: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchByOrder() {
        try {
            if (txtOrderID.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter Order ID to search", Alert.AlertType.WARNING);
                return;
            }

            orderDetailList.clear();
            orderDetailList.addAll(orderDetailController.getOrderDetailsByOrder(txtOrderID.getText().toUpperCase()));
            calculateTotalSales();
        } catch (Exception e) {
            showAlert("Error", "Error searching by order: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchByItem() {
        try {
            if (txtItemCode.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter Item Code to search", Alert.AlertType.WARNING);
                return;
            }

            orderDetailList.clear();
            orderDetailList.addAll(orderDetailController.getOrderDetailsByItem(txtItemCode.getText().toUpperCase()));
            calculateTotalSales();
        } catch (Exception e) {
            showAlert("Error", "Error searching by item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showPopularItems() {
        try {
            orderDetailList.clear();
            // This would show popular items in a different way
            // For now, let's show all items with their total sales
            loadAllOrderDetails();
            showAlert("Info", "Popular items feature would be implemented here", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Error showing popular items: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearFields() {
        txtOrderID.clear();
        txtItemCode.clear();
        txtOrderQty.clear();
        txtDiscount.clear();
        tblOrderDetails.getSelectionModel().clearSelection();
    }

    @FXML
    private void loadAll() {
        loadAllOrderDetails();
    }

    @FXML
    private void backToDashboard() {
        ((Stage) txtOrderID.getScene().getWindow()).close();
    }

    private void setOrderDetailData(OrderDetail orderDetail) {
        txtOrderID.setText(orderDetail.getOrderID());
        txtItemCode.setText(orderDetail.getItemCode());
        txtOrderQty.setText(String.valueOf(orderDetail.getOrderQty()));
        txtDiscount.setText(String.valueOf(orderDetail.getDiscount()));
    }

    private void loadAllOrderDetails() {
        try {
            orderDetailList.clear();
            orderDetailList.addAll(orderDetailController.getAllOrderDetails());
            calculateTotalSales();
        } catch (SQLException e) {
            showAlert("Error", "Error loading order details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void calculateTotalSales() {
        double totalSales = 0;
        for (OrderDetail detail : orderDetailList) {
            totalSales += detail.getTotal();
        }
        lblTotalSales.setText(String.format("Total Sales: Rs. %.2f", totalSales));
    }

    private boolean validateFields() {
        if (txtOrderID.getText().isEmpty() || txtItemCode.getText().isEmpty() ||
                txtOrderQty.getText().isEmpty() || txtDiscount.getText().isEmpty()) {

            showAlert("Validation Error", "Please fill all required fields", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int quantity = Integer.parseInt(txtOrderQty.getText());
            if (quantity <= 0) {
                showAlert("Validation Error", "Quantity must be greater than 0", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid quantity", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double discount = Double.parseDouble(txtDiscount.getText());
            if (discount < 0 || discount > 100) {
                showAlert("Validation Error", "Discount must be between 0 and 100", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid discount", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}