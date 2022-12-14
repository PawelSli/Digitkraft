package com.example.digitkraftbackend.dto;

import lombok.Data;

@Data
public class CopyDTO {

    private Integer id;
    private String code;
    private Boolean available;
    private ProductDTO product;
    private OrderDTO order;
}
