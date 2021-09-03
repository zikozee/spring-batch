package com.zikozee.batch.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */

@Getter @Setter @ToString
public class Product {
    private Integer productID;
    private String productName;
    private String productDesc;
    private BigDecimal price;
    private Integer unit;
}
