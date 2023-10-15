package com.toolbox.modbus.modbusapi;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.ModbusUtil;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class WriteDintToRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;

    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.WRITE_DINT_TO_REGISTERS.equals(command.getCommandType());
    }

    @Override
    public ModbusCommandResponse execute(ModbusCommand command) {
        int dintRegisterCount = 2;
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(command.getRegisterCount());
        response.setStatusCode(HttpStatus.OK.value());
        try {
            Long value = Long.parseLong(command.getData());
            client.writeRegisters(command.getRegisterOffset(),  Arrays.asList(client.toRegisterArray(  ModbusUtil.intToRegisters(value.intValue()))));
            Register[] registers =client.readRegisters(command.getRegisterOffset(), dintRegisterCount);
            value = client.registersToLong(registers);
            response.setData(value.toString());
        } catch (Exception e) {
            //log.error("Failed to execute.", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to write dint to registers.  Reason: " + e.getMessage());
        }
        return response;
    }

 
    protected Register[] shortToRegisterArray(Short value){
        return client.toRegisterArray(ModbusUtil.shortToRegister(value));
    }
}