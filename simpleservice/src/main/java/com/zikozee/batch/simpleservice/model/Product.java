package com.zikozee.batch.simpleservice.model;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    private Integer productId;
    private String prodName;
    private String productDesc;
    private BigDecimal price;
    private Integer unit;
}
