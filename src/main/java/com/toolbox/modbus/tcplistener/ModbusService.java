package com.toolbox.modbus.tcplistener;

import com.toolbox.modbus.tcplistener.ModbusServiceImpl.RegisterDto;

import net.wimpi.modbus.procimg.Register;

public interface ModbusService {
    public RegisterDto readRegisters(Integer startingAddress, Integer endingAddress) throws Exception ;
    public RegisterDto writeRegisters(Integer unitId,RegisterDto dto) throws Exception;
    public boolean readCoil(Integer coilAddress) throws Exception;
    public boolean writeCoil(Integer unitId, Integer coilAddress,Boolean value) throws Exception;
    
    public Register[] stringToRegisterArray(String value) ;
}
