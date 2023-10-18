package com.toolbox.modbus.modbusapi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModbusController {

    @Autowired
    private ModbusClient modbusClient;

    @PostMapping("/api/modbus/registers/read")
    public ModbusCommandResponse read(@RequestBody ModbusCommandRequest command) throws Exception {
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setStatusCode(200);
        response.setOffset(command.getOffset());
        response.setCount(command.getCount());
        response.setData(command.getData());
        response.setDataType(command.getDataType());
        if ("string".equalsIgnoreCase(command.getDataType())) {
            Optional.ofNullable(modbusClient.readStringFromRegisters(command.getOffset(), command.getCount()))
                    .ifPresent(response::setData);
        } else if ("high-byte".equalsIgnoreCase(command.getDataType())) {
            byte data = modbusClient.readHighByteFromRegister(command.getOffset());
            response.setData(modbusClient.toString(data));
        } else if ("low-byte".equalsIgnoreCase(command.getDataType())) {
            byte data = modbusClient.readLowByteFromRegister(command.getOffset());
            response.setData(modbusClient.toString(data));
        } else {
            Long data = modbusClient.readLongFromRegisters(command.getOffset());
            response.setData(String.valueOf(data));
        }
        return response;
    }

    @PostMapping("/api/modbus/registers/write")
    public ModbusCommandResponse write(@RequestBody ModbusCommandRequest command) throws Exception {
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setStatusCode(200);
        response.setOffset(command.getOffset());
        response.setCount(command.getCount());
        response.setData(command.getData());
        response.setDataType(command.getDataType());
        if ("string".equalsIgnoreCase(command.getDataType())) {
            modbusClient.writeRegisters(command.getOffset(), command.getData());
        } else if ("high-byte".equalsIgnoreCase(command.getDataType())) {
            modbusClient.writeHighByteRegister(command.getOffset(), modbusClient.toByte(command.getData()));
        } else if ("low-byte".equalsIgnoreCase(command.getDataType())) {
            modbusClient.writeLowByteRegister(command.getOffset(), modbusClient.toByte(command.getData()));
        } else {
            modbusClient.writeRegisters(command.getOffset(), Long.parseLong(command.getData()));
        }

        return response;
    }

    @PostMapping("/api/modbus/registers/clear")
    public ModbusCommandResponse clear(@RequestBody ModbusCommandRequest command) throws Exception {
        ModbusCommandResponse response = new ModbusCommandResponse();
        response.setStatusCode(200);
        response.setOffset(command.getOffset());
        response.setCount(command.getCount());
        response.setData(command.getData());
        response.setDataType(command.getDataType());

        modbusClient.clearRegisters(command.getOffset(), command.getCount());
        return response;
    }
}
