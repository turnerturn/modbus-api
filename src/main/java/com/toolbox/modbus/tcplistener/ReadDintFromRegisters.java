package com.toolbox.modbus.tcplistener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.wimpi.modbus.procimg.Register;

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
        int dintRegisterCount = 2;
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(dintRegisterCount);
        response.setStatusCode(HttpStatus.OK.value());
        try {
           Register[] registers =client.readRegisters(command.getRegisterOffset(), dintRegisterCount);
            Long value = client.registersToLong(registers);
            response.setData(value.toString());

        } catch (Exception e) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to read dint from registers.  Reason: "+System.lineSeparator() + e.getStackTrace());
        }
        return response;
    }

}