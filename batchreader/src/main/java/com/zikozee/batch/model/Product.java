package com.zikozee.batch.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */

@XmlRootElement(name = "product") // this is necessary for the xml binding
@XmlAccessorType(XmlAccessType.FIELD)  // map product by field
@Getter @Setter @ToString
public class Product {
    private Integer productId;

    @XmlElement(name = "productName")  //must match xml format: with this annotation field name can be anything
    private String prodName;
    private String productDesc;
    private BigDecimal price;
    private Integer unit;
}
