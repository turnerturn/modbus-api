package com.toolbox.modbus.tcplistener;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.wimpi.modbus.procimg.Register;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class WriteStringToRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;
@Autowired
private Toolbox     toolbox;
    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.WRITE_STRING_TO_REGISTERS.equals(command.getCommandType());
    }

    @Override
    public ModbusCommandResponse execute(ModbusCommand command) {
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(command.getRegisterCount());
        response.setStatusCode(HttpStatus.OK.value());
        try {
            Register[] registers = client.stringToRegisterArray(command.getData());
            client.writeRegisters(command.getRegisterOffset(), Arrays.asList(registers));
            response.setData(command.getData());
        } catch (Exception e) {
            //log.error("Failed to execute.", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to write string to registers.  Reason: " + e.getMessage());
        }
        return response;
    }



}
