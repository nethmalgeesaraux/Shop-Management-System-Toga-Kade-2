package Models.Dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDetail {
    private String orderID;
    private String itemCode;
    private int orderQty;
    private double discount;
}
