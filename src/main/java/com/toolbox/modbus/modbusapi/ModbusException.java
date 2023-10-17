package com.toolbox.modbus.modbusapi;

public class ModbusException extends Exception {
    
    public ModbusException(String message) {
        super(message);
    }

    public ModbusException(String message, Throwable cause) {
        super(message, cause);
    }
}
