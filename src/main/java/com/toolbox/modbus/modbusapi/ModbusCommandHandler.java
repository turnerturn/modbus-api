package com.toolbox.modbus.modbusapi;

public abstract class ModbusCommandHandler {
    public abstract boolean isMine(final ModbusCommand command);
    public abstract ModbusCommandResponse execute(final ModbusCommand command) ;
}
