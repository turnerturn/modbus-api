package com.toolbox.modbus.modbusapi;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class WriteLowByteToRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;

    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.WRITE_LOW_BYTE_TO_REGISTERS.equals(command.getCommandType());
    }

    @Override
    public ModbusCommandResponse execute(ModbusCommand command) {
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(command.getRegisterCount());
        response.setStatusCode(HttpStatus.OK.value());
        try {
            Register[] registers = client.readRegisters(command.getRegisterOffset(), 1);
          byte highByte =  ModbusUtil.hiByte(registers[0].getValue());
          byte lowByte  = client.toByte(command.getData());

          client.writeRegisters(command.getRegisterOffset(), Arrays.asList(new SimpleRegister(highByte, lowByte)));
        
        } catch (Exception e) {
            //log.error("Failed to execute.", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to write low byte to registers.  Reason: " + e.getMessage());
        }
        return response;
    }

  
}