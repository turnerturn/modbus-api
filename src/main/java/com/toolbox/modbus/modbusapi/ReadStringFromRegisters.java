package com.toolbox.modbus.modbusapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class ReadStringFromRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;

    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.READ_STRING_FROM_REGISTERS.equals(command.getCommandType());
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    class RegisterChunk{
        private Integer offset;
        private Integer count;
    }
    @Override
    public CommandResponse execute(ModbusCommand command)   {

        
        CommandResponse response = new CommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(command.getRegisterCount());
        response.setStatusCode(HttpStatus.OK.value());
        try {
          String data = client.readStringFromRegisters( command.getRegisterOffset(), command.getRegisterCount());
          response.setData(data);
        } catch (Exception e) {
            //log.error("Failed to execute.",e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to read string from registers.  Reason: "+ e.getMessage());
        }
          return response; 
    }


}