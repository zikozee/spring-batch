package com.zikozee.batch.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.math.BigDecimal;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */

//@XmlRootElement(name = "product") // this is necessary for the xml binding
//@XmlAccessorType(XmlAccessType.FIELD)  // map product by field
@Getter @Setter @ToString
public class Product {
    @XStreamAlias("ProdID")
    private Integer productId;

//    @XmlElement(name = "productName")
    private String prodName;
    private String productDesc;
    private BigDecimal price;
    private Integer unit;
}
