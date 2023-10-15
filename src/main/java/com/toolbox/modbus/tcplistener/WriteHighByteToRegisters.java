package com.toolbox.modbus.tcplistener;

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
public class WriteHighByteToRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;

    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.WRITE_HIGH_BYTE_TO_REGISTERS.equals(command.getCommandType());
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
            byte lowByte =  ModbusUtil.lowByte(registers[0].getValue());
            byte highByte  = client.convertStringToByte(command.getData());
    client.writeRegisters(command.getRegisterOffset(), Arrays.asList(new SimpleRegister(highByte, lowByte)));
        
         } catch (Exception e) {
            //log.error("Failed to execute.", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to write high byte to registers.  Reason: " + e.getMessage());
        }
        return response;
    }

}