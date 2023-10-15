package com.toolbox.modbus.modbusapi;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

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
    public ModbusCommandResponse execute(ModbusCommand command)   {

        
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setCommandType(command.getCommandType());
        response.setRegisterOffset(command.getRegisterOffset());
        response.setRegisterCount(command.getRegisterCount());
        response.setStatusCode(HttpStatus.OK.value());
        try {
        int lastRegisterOffset = command.getRegisterOffset() + command.getRegisterCount();
        int tmpRegisterOffset = command.getRegisterOffset();
        int tmpRegisterCount = (command.getRegisterCount() > 125)? 125 : command.getRegisterCount();
        StringBuilder sb = new StringBuilder();
        while(tmpRegisterOffset + tmpRegisterCount <= lastRegisterOffset){
           sb.append(registersArrayToString(client.readRegisters(tmpRegisterOffset, tmpRegisterCount)));       
            tmpRegisterOffset += tmpRegisterCount;
        }
        response.setData(sb.toString());
        } catch (Exception e) {
            //log.error("Failed to execute.",e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to read string from registers.  Reason: "+ e.getMessage());
        }
          return response; 
    }

    public String registersArrayToString(Register[] registers) throws Exception {
        //log.trace("toString(...)");
        Objects.requireNonNull(registers, "registers is null");
        ByteBuffer byteBuffer = ByteBuffer.allocate(registers.length * 2);
        for (Register register : registers) {
            byteBuffer.putShort(ModbusUtil.registerToShort(new SimpleRegister(register.getValue()).toBytes()));
        }
        String result = new String(byteBuffer.array(), StandardCharsets.US_ASCII);
        //log.debug("Registers was converted to string.  Value: {}", result);
        return result;
    }
}