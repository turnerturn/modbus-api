package com.toolbox.modbus.tcplistener;

public abstract class ModbusCommandHandler {
    public abstract boolean isMine(final ModbusCommand command);
    public abstract ModbusCommandResponse execute(final ModbusCommand command) ;
}
