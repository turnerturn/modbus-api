package com.toolbox.modbus.modbusapi;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class ModbusCommand {
    private ModbusCommandType commandType;
    private Integer registerOffset;
    private Integer registerCount;
    private String data;
    public ModbusCommand () {
}
    //constructor
    public ModbusCommand(ModbusCommandType commandType, Integer registerOffset, Integer registerCount, String data) {
        super();
        this.commandType = commandType;
        this.registerOffset = registerOffset;
        this.registerCount = registerCount;
        this.data = data;
    }
    //getters and setters
    public ModbusCommandType getCommandType() {
        return commandType;
    }
    public void setCommandType(ModbusCommandType commandType) {
        this.commandType = commandType;
    }
    public Integer getRegisterOffset() {
        return registerOffset;
    }
    public void setRegisterOffset(Integer registerOffset) {
        this.registerOffset = registerOffset;
    }
    public Integer getRegisterCount() {
        return registerCount;
    }
    public void setRegisterCount(Integer registerCount) {
        this.registerCount = registerCount;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
}

