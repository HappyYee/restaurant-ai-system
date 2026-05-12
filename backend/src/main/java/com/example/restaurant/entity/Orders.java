package com.example.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal originalAmount;
    private BigDecimal totalAmount;
    private Integer pointsUsed;
    private BigDecimal pointsDiscount;
    private Integer pointsEarned;
    private Integer status;
    private String remark;
    private Integer source;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
