package com.toolbox.modbus.tcplistener;

import net.wimpi.modbus.procimg.Register;

public interface ModbusService {
    public String readRegisters(int startingAddress, int endingAddress) throws Exception ;
    public String writeRegisters( int startingAddress, int endingAddress,String value) throws Exception;
    public boolean readCoil(int coilAddress) throws Exception;
    public boolean writeCoil(int coilAddress,boolean value) throws Exception;
    
    public Register[] stringToRegisterArray(String value) ;
}
