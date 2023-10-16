package com.toolbox.modbus.modbusapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class ReadDintFromRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;

    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.READ_DINT_FROM_REGISTERS.equals(command.getCommandType());
    }

    @Override
    public ModbusCommandResponse execute(ModbusCommand command) {
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(2);
        response.setStatusCode(HttpStatus.OK.value());
        try {
           Long value = client.readLongFromRegisters(command.getRegisterOffset());
            response.setData(value.toString());

        } catch (Exception e) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to read dint from registers.  Reason: "+System.lineSeparator() + e.getStackTrace());
        }
        return response;
    }

}