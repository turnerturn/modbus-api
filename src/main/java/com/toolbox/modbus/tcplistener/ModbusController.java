package com.toolbox.modbus.tcplistener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModbusController {
    @Autowired
    private ModbusClient modbusClient;

    @GetMapping("/api/modbus/registers/{offset}/{count}")
    public String readStringValueFromRegisters(
            @PathVariable("offset") Integer offset,
            @PathVariable("count") Integer count) throws Exception {
        return modbusClient.readStringValueFromRegisters(offset, count);
    }
    @PostMapping("/api/modbus/registers/{offset}/{count}")
    public String writeStringValueToRegisters(
            @PathVariable("offset") Integer offset,
            @PathVariable("count") Integer count, @RequestBody String value) throws Exception {
        return modbusClient.writeStringValueToRegisters(offset, count,value);
    }
    @GetMapping("/api/modbus/coils/{offset}")
    public Boolean readCoil( @PathVariable("offset") Integer offset)
            throws Exception {
        return modbusClient.readCoil(offset);
    }

    @PostMapping("/api/modbus/coils/{offset}")
    public String writeCoil( @PathVariable("offset") Integer offset,
            @RequestBody Boolean value) throws Exception {
        modbusClient.writeCoil( offset, value);
        return "success";
    }
}