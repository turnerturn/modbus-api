package com.toolbox.modbus.modbusapi;

public class ModbusIntegrationsException  extends Exception {

 
    /**
     * Constructs a ModbusIntegrationsException with no detail
     * message. A detail message is a String that describes this
     * particular exception.
     */
    public ModbusIntegrationsException() {
        super();
    }

    /**
     * Constructs a ModbusIntegrationsException with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public ModbusIntegrationsException(String msg, Throwable e) {
        super(msg,e);
    }
}
