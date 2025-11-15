package Models.Dto;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Customer {
    private String custID;
    private String custTitle;
    private String custName;
    private LocalDate dob;
    private double salary;
    private String custAddress;
    private String city;
    private String province;
    private String postalCode;
}
