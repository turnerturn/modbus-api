package com.toolbox.modbus.tcplistener;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Register {
    private String name;
    private String type;
    private Integer offset;
    private Integer count;
    private String description;

}
