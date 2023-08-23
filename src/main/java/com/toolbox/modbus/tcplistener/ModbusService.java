package com.toolbox.modbus.tcplistener;

import java.util.List;

import com.toolbox.modbus.tcplistener.ModbusServiceImpl.RegisterDto;

import net.wimpi.modbus.procimg.Register;

public interface ModbusService {
    public RegisterDto readRegisters(Integer startingAddress, Integer endingAddress) throws Exception ;
    public void writeRegisterGroups(Integer unitId,List<RegisterDto> registerGroups) throws Exception;
   public void writeValueToRegisterGroup(Integer unitId,Integer offset,Integer count, String value) throws Exception;

    public boolean readCoil(Integer offset) throws Exception;
    public boolean writeCoil(Integer unitId, Integer offset,Boolean value) throws Exception;
    
    public Register[] stringToRegisterArray(String value) ;
}