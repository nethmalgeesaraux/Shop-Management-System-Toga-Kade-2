package Controller;

import Models.Db.DatabaseConnection;
import Models.Dto.Order;
import Models.Dto.OrderDetail;
import Models.Dto.Item;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderController {

    public boolean placeOrder(Order order, List<OrderDetail> orderDetails) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();

        try {
            connection.setAutoCommit(false);

            // 1. Save order
            String orderSql = "INSERT INTO Orders (OrderID, OrderDate, CustID) VALUES (?, ?, ?)";
            PreparedStatement orderStmt = connection.prepareStatement(orderSql);
            orderStmt.setString(1, order.getOrderID());
            orderStmt.setDate(2, Date.valueOf(order.getOrderDate()));
            orderStmt.setString(3, order.getCustID());
            orderStmt.executeUpdate();

            // 2. Save order details and update item quantities
            String detailSql = "INSERT INTO OrderDetail (OrderID, ItemCode, OrderQTY, Discount) VALUES (?, ?, ?, ?)";
            String updateItemSql = "UPDATE Item SET QtyOnHand = QtyOnHand - ? WHERE ItemCode = ?";

            PreparedStatement detailStmt = connection.prepareStatement(detailSql);
            PreparedStatement updateStmt = connection.prepareStatement(updateItemSql);

            for (OrderDetail detail : orderDetails) {
                // Check stock availability
                String checkStockSql = "SELECT QtyOnHand FROM Item WHERE ItemCode = ?";
                PreparedStatement checkStockStmt = connection.prepareStatement(checkStockSql);
                checkStockStmt.setString(1, detail.getItemCode());
                ResultSet rs = checkStockStmt.executeQuery();

                if (rs.next()) {
                    int availableQty = rs.getInt("QtyOnHand");
                    if (availableQty < detail.getOrderQty()) {
                        throw new SQLException("Insufficient stock for item: " + detail.getItemCode() +
                                ". Available: " + availableQty + ", Requested: " + detail.getOrderQty());
                    }
                } else {
                    throw new SQLException("Item not found: " + detail.getItemCode());
                }

                // Save order detail
                detailStmt.setString(1, detail.getOrderID());
                detailStmt.setString(2, detail.getItemCode());
                detailStmt.setInt(3, detail.getOrderQty());
                detailStmt.setDouble(4, detail.getDiscount());
                detailStmt.addBatch();

                // Update item quantity
                updateStmt.setInt(1, detail.getOrderQty());
                updateStmt.setString(2, detail.getItemCode());
                updateStmt.addBatch();
            }

            // Execute batches
            detailStmt.executeBatch();
            updateStmt.executeBatch();

            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM Orders ORDER BY OrderDate DESC, OrderID";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            Order order = new Order();
            order.setOrderID(rs.getString("OrderID"));
            order.setOrderDate(rs.getDate("OrderDate").toLocalDate());
            order.setCustID(rs.getString("CustID"));
            orders.add(order);
        }
        return orders;
    }

    public Order getOrderById(String orderID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM Orders WHERE OrderID = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Order order = new Order();
            order.setOrderID(rs.getString("OrderID"));
            order.setOrderDate(rs.getDate("OrderDate").toLocalDate());
            order.setCustID(rs.getString("CustID"));
            return order;
        }
        return null;
    }

    public List<OrderDetail> getOrderDetails(String orderID) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT od.*, i.Description, i.UnitPrice " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "WHERE od.OrderID = ? " +
                "ORDER BY od.ItemCode";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderID(rs.getString("OrderID"));
            detail.setItemCode(rs.getString("ItemCode"));
            detail.setOrderQty(rs.getInt("OrderQTY"));
            detail.setDiscount(rs.getDouble("Discount"));
            detail.setDescription(rs.getString("Description"));
            detail.setUnitPrice(rs.getDouble("UnitPrice"));
            details.add(detail);
        }
        return details;
    }

    public String generateNextOrderId() throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT OrderID FROM Orders ORDER BY OrderID DESC LIMIT 1";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            String lastId = rs.getString("OrderID");
            try {
                int nextNum = Integer.parseInt(lastId.substring(1)) + 1;
                return String.format("D%03d", nextNum);
            } catch (NumberFormatException e) {
                return "D001";
            }
        } else {
            return "D001";
        }
    }

    public boolean deleteOrder(String orderID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();

        try {
            connection.setAutoCommit(false);

            // 1. Restore item quantities
            String getDetailsSql = "SELECT ItemCode, OrderQTY FROM OrderDetail WHERE OrderID = ?";
            PreparedStatement getDetailsStmt = connection.prepareStatement(getDetailsSql);
            getDetailsStmt.setString(1, orderID);
            ResultSet rs = getDetailsStmt.executeQuery();

            String restoreStockSql = "UPDATE Item SET QtyOnHand = QtyOnHand + ? WHERE ItemCode = ?";
            PreparedStatement restoreStmt = connection.prepareStatement(restoreStockSql);

            while (rs.next()) {
                restoreStmt.setInt(1, rs.getInt("OrderQTY"));
                restoreStmt.setString(2, rs.getString("ItemCode"));
                restoreStmt.addBatch();
            }
            restoreStmt.executeBatch();

            // 2. Delete order details
            String deleteDetailsSql = "DELETE FROM OrderDetail WHERE OrderID = ?";
            PreparedStatement deleteDetailsStmt = connection.prepareStatement(deleteDetailsSql);
            deleteDetailsStmt.setString(1, orderID);
            deleteDetailsStmt.executeUpdate();

            // 3. Delete order
            String deleteOrderSql = "DELETE FROM Orders WHERE OrderID = ?";
            PreparedStatement deleteOrderStmt = connection.prepareStatement(deleteOrderSql);
            deleteOrderStmt.setString(1, orderID);
            int result = deleteOrderStmt.executeUpdate();

            connection.commit();
            return result > 0;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public double getOrderTotal(String orderID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT SUM(od.OrderQTY * i.UnitPrice * (1 - od.Discount/100)) as Total " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "WHERE od.OrderID = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getDouble("Total");
        }
        return 0.0;
    }

    public boolean orderExists(String orderID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT OrderID FROM Orders WHERE OrderID = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        ResultSet rs = stmt.executeQuery();

        return rs.next();
    }

    // ... other methods
}