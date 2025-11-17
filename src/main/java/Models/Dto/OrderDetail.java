package Models.Dto;

public class OrderDetail {
    private String orderID;
    private String itemCode;
    private int orderQty;
    private double discount;
    private String description;
    private double unitPrice;

    public OrderDetail() {}

    public OrderDetail(String orderID, String itemCode, int orderQty, double discount) {
        this.orderID = orderID;
        this.itemCode = itemCode;
        this.orderQty = orderQty;
        this.discount = discount;
    }


    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public int getOrderQty() { return orderQty; }
    public void setOrderQty(int orderQty) { this.orderQty = orderQty; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }


    public double getTotal() {
        return orderQty * unitPrice * (1 - discount / 100);
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderID='" + orderID + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", orderQty=" + orderQty +
                ", discount=" + discount +
                ", description='" + description + '\'' +
                ", unitPrice=" + unitPrice +
                '}';
    }
}