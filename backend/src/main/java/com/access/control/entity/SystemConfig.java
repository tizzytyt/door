package com.access.control.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    private Integer id;
    private String configKey;
    private String configValue;
    private String description;
}
