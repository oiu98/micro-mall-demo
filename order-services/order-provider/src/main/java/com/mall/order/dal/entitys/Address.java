package com.mall.order.dal.entitys;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author ZhaoJiachen on 2021/5/27
 * <p>
 * Description:
 */

@Table(name = "tb_address")
@Data
public class Address {

    @Id
    private Long addressId;

    private Long userId;

    private String userName;

    private String tel;

    private String streetName;
}
