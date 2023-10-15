package com.toolbox.modbus.modbusapi;

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
public class ReadHighByteFromRegisters extends ModbusCommandHandler {
    @Autowired
    private ModbusClient client;

    @Override
    public boolean isMine(ModbusCommand command) {
        return ModbusCommandType.READ_HIGH_BYTE_FROM_REGISTERS.equals(command.getCommandType());
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
            response.setData( convertByteToString(highByte));
        } catch (Exception e) {
            //log.error("Failed to execute.", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to read high byte from register.  Reason: " + e.getMessage());
        }
        return response;
    }    
    
    protected  byte convertStringToByte(String input) {
        byte result = 0;

      for (int i = 0; i < 8; i++) {
            char c = input.charAt(i);
            if (c == '1') {
                result |= (1 << (i));
            } else if (c != '0') {
                throw new IllegalArgumentException("Input string contains invalid characters: " + input);
            }
        }

        return result;
    }

    public  String convertByteToString(byte inputByte) {
        StringBuilder stringBuilder = new StringBuilder(8);

        // Iterate through each bit in the byte
        for (int i = 0; i < 8; i++) {
            // Use bitwise AND operation to check the value of the bit
            byte bitValue = (byte) ((inputByte >> i) & 1);

            // Append '0' or '1' to the StringBuilder
            stringBuilder.append(bitValue);
        }

        return stringBuilder.toString();
    }
}