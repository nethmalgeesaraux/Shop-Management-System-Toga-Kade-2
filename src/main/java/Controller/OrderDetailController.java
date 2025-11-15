package Controller;

import Models.Db.DatabaseConnection;
import Models.Dto.OrderDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailController {

    public boolean saveOrderDetail(OrderDetail orderDetail) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "INSERT INTO OrderDetail (OrderID, ItemCode, OrderQTY, Discount) VALUES (?, ?, ?, ?)";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderDetail.getOrderID());
        stmt.setString(2, orderDetail.getItemCode());
        stmt.setInt(3, orderDetail.getOrderQty());
        stmt.setDouble(4, orderDetail.getDiscount());

        return stmt.executeUpdate() > 0;
    }

    public boolean updateOrderDetail(OrderDetail orderDetail) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "UPDATE OrderDetail SET OrderQTY=?, Discount=? WHERE OrderID=? AND ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, orderDetail.getOrderQty());
        stmt.setDouble(2, orderDetail.getDiscount());
        stmt.setString(3, orderDetail.getOrderID());
        stmt.setString(4, orderDetail.getItemCode());

        return stmt.executeUpdate() > 0;
    }

    public boolean deleteOrderDetail(String orderID, String itemCode) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "DELETE FROM OrderDetail WHERE OrderID=? AND ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        stmt.setString(2, itemCode);

        return stmt.executeUpdate() > 0;
    }

    public boolean deleteAllOrderDetails(String orderID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "DELETE FROM OrderDetail WHERE OrderID=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);

        return stmt.executeUpdate() > 0;
    }

    public OrderDetail searchOrderDetail(String orderID, String itemCode) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT od.*, i.Description, i.UnitPrice " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "WHERE od.OrderID=? AND od.ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        stmt.setString(2, itemCode);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            OrderDetail detail = new OrderDetail(
                    rs.getString("OrderID"),
                    rs.getString("ItemCode"),
                    rs.getInt("OrderQTY"),
                    rs.getDouble("Discount")
            );
            detail.setDescription(rs.getString("Description"));
            detail.setUnitPrice(rs.getDouble("UnitPrice"));
            return detail;
        }
        return null;
    }

    public List<OrderDetail> getOrderDetailsByOrder(String orderID) throws SQLException {
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
            OrderDetail detail = new OrderDetail(
                    rs.getString("OrderID"),
                    rs.getString("ItemCode"),
                    rs.getInt("OrderQTY"),
                    rs.getDouble("Discount")
            );
            detail.setDescription(rs.getString("Description"));
            detail.setUnitPrice(rs.getDouble("UnitPrice"));
            details.add(detail);
        }
        return details;
    }

    public List<OrderDetail> getOrderDetailsByItem(String itemCode) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT od.*, i.Description, i.UnitPrice " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "WHERE od.ItemCode = ? " +
                "ORDER BY od.OrderID";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, itemCode);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            OrderDetail detail = new OrderDetail(
                    rs.getString("OrderID"),
                    rs.getString("ItemCode"),
                    rs.getInt("OrderQTY"),
                    rs.getDouble("Discount")
            );
            detail.setDescription(rs.getString("Description"));
            detail.setUnitPrice(rs.getDouble("UnitPrice"));
            details.add(detail);
        }
        return details;
    }

    public List<OrderDetail> getAllOrderDetails() throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT od.*, i.Description, i.UnitPrice " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "ORDER BY od.OrderID, od.ItemCode";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            OrderDetail detail = new OrderDetail(
                    rs.getString("OrderID"),
                    rs.getString("ItemCode"),
                    rs.getInt("OrderQTY"),
                    rs.getDouble("Discount")
            );
            detail.setDescription(rs.getString("Description"));
            detail.setUnitPrice(rs.getDouble("UnitPrice"));
            details.add(detail);
        }
        return details;
    }

    public double getOrderTotalAmount(String orderID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT SUM(OrderQTY * UnitPrice * (1 - Discount/100)) as TotalAmount " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "WHERE od.OrderID = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getDouble("TotalAmount");
        }
        return 0.0;
    }

    public int getTotalQuantitySold(String itemCode) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT SUM(OrderQTY) as TotalQty FROM OrderDetail WHERE ItemCode = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, itemCode);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("TotalQty");
        }
        return 0;
    }

    public double getTotalSalesByItem(String itemCode) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT SUM(od.OrderQTY * i.UnitPrice * (1 - od.Discount/100)) as TotalSales " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "WHERE od.ItemCode = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, itemCode);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getDouble("TotalSales");
        }
        return 0.0;
    }

    public List<String> getPopularItems(int limit) throws SQLException {
        List<String> popularItems = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT od.ItemCode, i.Description, SUM(od.OrderQTY) as TotalQty " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "GROUP BY od.ItemCode, i.Description " +
                "ORDER BY TotalQty DESC " +
                "LIMIT ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, limit);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String itemInfo = rs.getString("ItemCode") + " - " +
                    rs.getString("Description") + " (Sold: " +
                    rs.getInt("TotalQty") + ")";
            popularItems.add(itemInfo);
        }
        return popularItems;
    }

    public boolean updateOrderDetailQuantity(String orderID, String itemCode, int newQuantity) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();

        try {
            connection.setAutoCommit(false);

            // Get current quantity
            String getCurrentQtySql = "SELECT OrderQTY FROM OrderDetail WHERE OrderID=? AND ItemCode=?";
            PreparedStatement getStmt = connection.prepareStatement(getCurrentQtySql);
            getStmt.setString(1, orderID);
            getStmt.setString(2, itemCode);
            ResultSet rs = getStmt.executeQuery();

            if (rs.next()) {
                int currentQty = rs.getInt("OrderQTY");
                int quantityDifference = newQuantity - currentQty;

                // Update order detail
                String updateSql = "UPDATE OrderDetail SET OrderQTY=? WHERE OrderID=? AND ItemCode=?";
                PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                updateStmt.setInt(1, newQuantity);
                updateStmt.setString(2, orderID);
                updateStmt.setString(3, itemCode);
                updateStmt.executeUpdate();

                // Update item stock
                if (quantityDifference != 0) {
                    String updateStockSql = "UPDATE Item SET QtyOnHand = QtyOnHand - ? WHERE ItemCode = ?";
                    PreparedStatement stockStmt = connection.prepareStatement(updateStockSql);
                    stockStmt.setInt(1, quantityDifference);
                    stockStmt.setString(2, itemCode);
                    stockStmt.executeUpdate();
                }

                connection.commit();
                return true;
            }

            connection.rollback();
            return false;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean updateOrderDetailDiscount(String orderID, String itemCode, double newDiscount) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "UPDATE OrderDetail SET Discount=? WHERE OrderID=? AND ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setDouble(1, newDiscount);
        stmt.setString(2, orderID);
        stmt.setString(3, itemCode);

        return stmt.executeUpdate() > 0;
    }

    public List<OrderDetail> getOrderDetailsWithCustomerInfo(String orderID) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT od.*, i.Description, i.UnitPrice, o.OrderDate, c.CustName " +
                "FROM OrderDetail od " +
                "JOIN Item i ON od.ItemCode = i.ItemCode " +
                "JOIN Orders o ON od.OrderID = o.OrderID " +
                "JOIN Customer c ON o.CustID = c.CustID " +
                "WHERE od.OrderID = ? " +
                "ORDER BY od.ItemCode";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, orderID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            OrderDetail detail = new OrderDetail(
                    rs.getString("OrderID"),
                    rs.getString("ItemCode"),
                    rs.getInt("OrderQTY"),
                    rs.getDouble("Discount")
            );
            detail.setDescription(rs.getString("Description"));
            detail.setUnitPrice(rs.getDouble("UnitPrice"));
            // Store additional info in description field
            detail.setDescription(detail.getDescription() + " | Order Date: " +
                    rs.getDate("OrderDate") + " | Customer: " +
                    rs.getString("CustName"));
            details.add(detail);
        }
        return details;
    }
}