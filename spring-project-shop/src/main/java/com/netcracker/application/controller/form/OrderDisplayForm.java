package com.netcracker.application.controller.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDisplayForm {
    private BigInteger orderId;
    private String username;
    private BigInteger userId;
    private Double totalSum;
    private Timestamp creationDate;
    private String customerName;
    private String customerAddress;
    private String customerPhoneNumber;
}
