package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import Models.Dto.Customer;
import Models.Dto.Item;
import Models.Dto.Order;
import Models.Dto.OrderDetail;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OrderFormController implements Initializable {

    @FXML private TextField txtOrderID, txtOrderQty, txtDiscount;
    @FXML private DatePicker dpOrderDate;
    @FXML private ComboBox<String> cmbCustomer, cmbItem;
    @FXML private TableView<OrderItem> tblOrderItems;
    @FXML private TableView<Order> tblOrders;
    @FXML private Label lblTotalAmount;

    // Order Items Table Columns
    @FXML private TableColumn<OrderItem, String> colItemCodeOrder, colDescriptionOrder;
    @FXML private TableColumn<OrderItem, Integer> colQtyOrder;
    @FXML private TableColumn<OrderItem, Double> colUnitPriceOrder, colDiscountOrder, colTotalOrder;
    @FXML private TableColumn<OrderItem, String> colActionOrder;

    // Orders History Table Columns
    @FXML private TableColumn<Order, String> colOrderID, colCustomerID, colCustomerName;
    @FXML private TableColumn<Order, LocalDate> colOrderDate;
    @FXML private TableColumn<Order, Double> colOrderTotal;
    @FXML private TableColumn<Order, String> colOrderAction;

    private OrderController orderController;
    private CustomerController customerController;
    private ItemController itemController;
    private ObservableList<OrderItem> orderItemsList;
    private ObservableList<Order> ordersList;
    private ObservableList<String> customerIDs;
    private ObservableList<String> itemCodes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize controllers
        orderController = new OrderController();
        customerController = new CustomerController();
        itemController = new ItemController();

        // Initialize observable lists
        orderItemsList = FXCollections.observableArrayList();
        ordersList = FXCollections.observableArrayList();
        customerIDs = FXCollections.observableArrayList();
        itemCodes = FXCollections.observableArrayList();

        // Set default values
        dpOrderDate.setValue(LocalDate.now());
        txtDiscount.setText("0");

        // Initialize UI components
        initializeOrderItemsTable();
        initializeOrdersTable();
        loadCustomers();
        loadItems();
        loadAllOrders();

        // Generate initial order ID
        generateOrderId();
    }

    private void initializeOrderItemsTable() {
        // Initialize order items table columns
        if (colItemCodeOrder != null) {
            colItemCodeOrder.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        }
        if (colDescriptionOrder != null) {
            colDescriptionOrder.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (colQtyOrder != null) {
            colQtyOrder.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        }
        if (colUnitPriceOrder != null) {
            colUnitPriceOrder.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        }
        if (colDiscountOrder != null) {
            colDiscountOrder.setCellValueFactory(new PropertyValueFactory<>("discount"));
        }
        if (colTotalOrder != null) {
            colTotalOrder.setCellValueFactory(new PropertyValueFactory<>("total"));
        }

        // Add remove button to action column
        if (colActionOrder != null) {
            colActionOrder.setCellFactory(param -> new TableCell<OrderItem, String>() {
                private final Button removeButton = new Button("Remove");

                {
                    removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    removeButton.setOnAction(event -> {
                        OrderItem item = getTableView().getItems().get(getIndex());
                        orderItemsList.remove(item);
                        calculateTotal();
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(removeButton);
                    }
                }
            });
        }

        tblOrderItems.setItems(orderItemsList);
    }

    private void initializeOrdersTable() {
        // Initialize orders history table columns
        if (colOrderID != null) {
            colOrderID.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        }
        if (colOrderDate != null) {
            colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        }
        if (colCustomerID != null) {
            colCustomerID.setCellValueFactory(new PropertyValueFactory<>("custID"));
        }
        if (colCustomerName != null) {
            colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        }
        if (colOrderTotal != null) {
            colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        }

        // Add view details button to action column
        if (colOrderAction != null) {
            colOrderAction.setCellFactory(param -> new TableCell<Order, String>() {
                private final Button viewButton = new Button("View Details");
                private final Button deleteButton = new Button("Delete");
                private final HBox buttonBox = new HBox(5, viewButton, deleteButton);

                {
                    viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10;");
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");

                    viewButton.setOnAction(event -> {
                        Order order = getTableView().getItems().get(getIndex());
                        viewOrderDetails(order.getOrderID());
                    });

                    deleteButton.setOnAction(event -> {
                        Order order = getTableView().getItems().get(getIndex());
                        deleteOrder(order.getOrderID());
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonBox);
                    }
                }
            });
        }

        tblOrders.setItems(ordersList);
    }

    @FXML
    private void generateOrderId() {
        try {
            txtOrderID.setText(orderController.generateNextOrderId());
        } catch (SQLException e) {
            showAlert("Error", "Error generating order ID: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void addOrderItem() {
        try {
            if (validateOrderItem()) {
                String selectedItem = cmbItem.getValue();
                if (selectedItem != null) {
                    String itemCode = selectedItem.split(" - ")[0];
                    Item item = itemController.searchItem(itemCode);

                    if (item != null) {
                        int quantity = Integer.parseInt(txtOrderQty.getText());
                        double discount = Double.parseDouble(txtDiscount.getText());

                        // Check if item already exists in order
                        for (OrderItem orderItem : orderItemsList) {
                            if (orderItem.getItemCode().equals(itemCode)) {
                                orderItem.setQuantity(orderItem.getQuantity() + quantity);
                                tblOrderItems.refresh();
                                calculateTotal();
                                clearOrderItemFields();
                                return;
                            }
                        }

                        // Add new item
                        OrderItem orderItem = new OrderItem(
                                itemCode,
                                item.getDescription(),
                                quantity,
                                item.getUnitPrice(),
                                discount
                        );

                        orderItemsList.add(orderItem);
                        calculateTotal();
                        clearOrderItemFields();
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error adding item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void placeOrder() {
        try {
            if (validateOrder()) {
                String selectedCustomer = cmbCustomer.getValue();
                if (selectedCustomer != null) {
                    String custID = selectedCustomer.split(" - ")[0];


                    Order order = new Order();
                    order.setOrderID(txtOrderID.getText());
                    order.setOrderDate(dpOrderDate.getValue());
                    order.setCustID(custID);

                    List<OrderDetail> orderDetails = new ArrayList<>();
                    for (OrderItem orderItem : orderItemsList) {
                        OrderDetail detail = new OrderDetail(
                                order.getOrderID(),
                                orderItem.getItemCode(),
                                orderItem.getQuantity(),
                                orderItem.getDiscount()
                        );
                        orderDetails.add(detail);
                    }

                    // Use the fixed placeOrder method
                    if (orderController.placeOrder(order, orderDetails)) {
                        showAlert("Success", "Order placed successfully!", Alert.AlertType.INFORMATION);
                        clearOrder();
                        loadAllOrders();
                        generateOrderId();
                    } else {
                        showAlert("Error", "Failed to place order!", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error placing order: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    private void clearOrder() {
        orderItemsList.clear();
        clearOrderItemFields();
        calculateTotal();
    }

    @FXML
    private void backToDashboard() {
        ((Stage) txtOrderID.getScene().getWindow()).close();
    }

    private void loadCustomers() {
        try {
            customerIDs.clear();
            List<Customer> customers = customerController.getAllCustomers();
            for (Customer customer : customers) {
                customerIDs.add(customer.getCustID() + " - " + customer.getCustName());
            }
            cmbCustomer.setItems(customerIDs);
        } catch (SQLException e) {
            showAlert("Error", "Error loading customers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadItems() {
        try {
            itemCodes.clear();
            List<Item> items = itemController.getAllItems();
            for (Item item : items) {
                if (item.getQtyOnHand() > 0) {
                    itemCodes.add(item.getItemCode() + " - " + item.getDescription() + " (Stock: " + item.getQtyOnHand() + ")");
                }
            }
            cmbItem.setItems(itemCodes);
        } catch (SQLException e) {
            showAlert("Error", "Error loading items: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAllOrders() {
        try {
            ordersList.clear();
            List<Order> orders = orderController.getAllOrders();

            // Add customer names and totals to orders
            for (Order order : orders) {
                String customerName = customerController.getCustomerName(order.getCustID());
                order.setCustID(customerName != null ? customerName : "Unknown");

                double total = orderController.getOrderTotal(order.getOrderID());
                order.setTotal(total);

                ordersList.add(order);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading orders: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void viewOrderDetails(String orderID) {
        try {
            // You can implement a separate order details view here
            showAlert("Order Details", "Viewing details for order: " + orderID, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Error viewing order details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteOrder(String orderID) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Order");
            confirmation.setContentText("Are you sure you want to delete order " + orderID + "?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                if (orderController.deleteOrder(orderID)) {
                    showAlert("Success", "Order deleted successfully!", Alert.AlertType.INFORMATION);
                    loadAllOrders();
                } else {
                    showAlert("Error", "Failed to delete order!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error deleting order: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void calculateTotal() {
        double total = 0;
        for (OrderItem item : orderItemsList) {
            total += item.getTotal();
        }
        lblTotalAmount.setText(String.format("Rs. %.2f", total));
    }

    private void clearOrderItemFields() {
        cmbItem.getSelectionModel().clearSelection();
        txtOrderQty.clear();
        txtDiscount.setText("0");
    }

    private boolean validateOrderItem() {
        if (cmbItem.getValue() == null || txtOrderQty.getText().isEmpty()) {
            showAlert("Validation Error", "Please select item and enter quantity", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int quantity = Integer.parseInt(txtOrderQty.getText());
            if (quantity <= 0) {
                showAlert("Validation Error", "Quantity must be greater than 0", Alert.AlertType.WARNING);
                return false;
            }

            // Check stock availability
            String itemCode = cmbItem.getValue().split(" - ")[0];
            Item item = itemController.searchItem(itemCode);
            if (item != null && quantity > item.getQtyOnHand()) {
                showAlert("Stock Error", "Insufficient stock! Available: " + item.getQtyOnHand(), Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid quantity", Alert.AlertType.WARNING);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    private boolean validateOrder() {
        if (txtOrderID.getText().isEmpty() || dpOrderDate.getValue() == null ||
                cmbCustomer.getValue() == null || orderItemsList.isEmpty()) {

            showAlert("Validation Error", "Please fill all order details and add at least one item", Alert.AlertType.WARNING);
            return false;
        }

        if (dpOrderDate.getValue().isAfter(LocalDate.now())) {
            showAlert("Validation Error", "Order date cannot be in the future", Alert.AlertType.WARNING);
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

    // Inner class for order items table
    public static class OrderItem {
        private String itemCode;
        private String description;
        private int quantity;
        private double unitPrice;
        private double discount;

        public OrderItem(String itemCode, String description, int quantity, double unitPrice, double discount) {
            this.itemCode = itemCode;
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discount = discount;
        }

        public String getItemCode() { return itemCode; }
        public String getDescription() { return description; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getDiscount() { return discount; }
        public double getTotal() {
            return quantity * unitPrice * (1 - discount / 100);
        }

        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}