package com.toolbox.modbus.modbusapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
    public CommandResponse execute(ModbusCommand command) {
        CommandResponse response = new CommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(2);
        response.setStatusCode(HttpStatus.OK.value());
        try {
            
            client.writeRegisters(command.getRegisterOffset(), Long.parseLong(command.getData()));
        } catch (Exception e) {
            //log.error("Failed to execute.", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to write dint to registers.  Reason: " + e.getMessage());
        }
        return response;
    }
}