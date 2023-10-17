package com.toolbox.modbus.modbusapi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class ClearMemoryFromRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;
@Autowired
private Toolbox     toolbox;
    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.CLEAR_MEMORY_FROM_REGISTERS.equals(command.getCommandType());
    }

    @Override
    public CommandResponse execute(ModbusCommand command) {
        CommandResponse response = new CommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(command.getRegisterCount());
        response.setStatusCode(HttpStatus.OK.value());
        try {
            List<Register> registers = new ArrayList<>();
            toolbox.fillList(registers, new SimpleRegister(0), command.getRegisterCount());
            client.writeRegisters(command.getRegisterOffset(),registers);
        } catch (Exception e) {
            //log.error("Failed to execute.", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to write string to registers.  Reason: " + e.getMessage());
        }
        return response;
    }



}
