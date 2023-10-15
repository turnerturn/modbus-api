package com.toolbox.modbus.tcplistener;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class ModbusCommandResponse {
    private ModbusCommandType commandType;
    private Integer registerOffset;
    private Integer registerCount;
    private String data;
    private Integer statusCode;
    private String message;
    //constructor
    public ModbusCommandResponse() {
    }
    public ModbusCommandResponse(ModbusCommandType commandType, Integer registerOffset, Integer registerCount, String data, Integer statusCode, String message) {
        super();
        this.commandType = commandType;
        this.registerOffset = registerOffset;
        this.registerCount = registerCount;
        this.data = data;
        this.statusCode = statusCode;
        this.message = message;
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
    public Integer getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
}
