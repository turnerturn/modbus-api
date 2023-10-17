package com.toolbox.modbus.modbusapi;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ModbusCommandRequest {
    private Integer offset;
    private Integer count;
    private String data;
    private String dataType;
}