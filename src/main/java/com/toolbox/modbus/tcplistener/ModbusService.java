package com.toolbox.modbus.tcplistener;

import java.util.List;

import com.toolbox.modbus.tcplistener.ModbusServiceImpl.RegisterDto;

import net.wimpi.modbus.procimg.Register;

public interface ModbusService {
    public String readHoldingRegistersValue(Integer offset, Integer count) throws Exception ;
    public void  writeHoldingRegistersValue(Integer unitId,Integer offset, Integer count, String value) throws Exception;
    public void  uploadRegisters(Integer unitId,List<RegisterDto> registers) throws Exception;
    
    public boolean readCoil(Integer offset) throws Exception;
    public boolean writeCoil(Integer unitId, Integer offset,Boolean value) throws Exception;
    
    public Register[] stringToRegisterArray(String value) ;
}
