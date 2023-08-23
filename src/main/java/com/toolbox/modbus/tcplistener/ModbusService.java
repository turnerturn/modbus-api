package com.toolbox.modbus.tcplistener;

import java.util.List;

import com.toolbox.modbus.tcplistener.ModbusServiceImpl.RegisterDto;

import net.wimpi.modbus.procimg.Register;

public interface ModbusService {
    public String readValueFromRegisters(Integer offset, Integer count) throws Exception ;
    public void  writeRegisters(Integer unitId,List<RegisterDto> registers) throws Exception;
    public boolean readCoil(Integer coilAddress) throws Exception;
    public boolean writeCoil(Integer unitId, Integer coilAddress,Boolean value) throws Exception;
    
    public Register[] stringToRegisterArray(String value) ;
}
