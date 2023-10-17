package com.toolbox.modbus.modbusapi;

public abstract class ModbusCommandHandler {
    public abstract boolean isMine(final ModbusCommand command);
    public abstract CommandResponse execute(final ModbusCommand command) ;
}
