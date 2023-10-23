package com.toolbox.modbus.modbusapi;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RestController
public class ModbusController {

    @Autowired
    private ModbusClient modbusClient;
    @DeleteMapping("/api/modbus/registers/{offset}")
    public String clear(@PathVariable Integer offset) throws Exception {
        modbusClient.clearRegisters(offset, 1);
        return "ok";
    }
    @DeleteMapping("/api/modbus/registers/{offset}/{count}")
    public String clear(@PathVariable Integer offset,@PathVariable Integer count) throws Exception {
        modbusClient.clearRegisters(offset, count);
        return "ok";
    }
    @PostMapping("/api/modbus/registers/{offset}")
    public String writeString(@PathVariable Integer offset, @RequestBody String value) throws Exception {
        modbusClient.writeRegisters(offset, value);
        return "ok";
    }

    @PostMapping("/api/modbus/registers/{offset}/dint")
    public String writeDint(@PathVariable Integer offset, @RequestBody Long value) throws Exception {
        modbusClient.writeRegisters(offset, value);
        return "ok";
    }

    @PostMapping("/api/modbus/registers/{offset}/high-byte")
    public String writeHighByte(@PathVariable Integer offset, @RequestBody String value) throws ModbusException {
        modbusClient.writeHighByteRegister(offset, value);
        return "ok";
    }

    @PostMapping("/api/modbus/registers/{offset}/low-byte")
    public String writeLowByte(@PathVariable Integer offset, @RequestBody String value) throws ModbusException {
        modbusClient.writeLowByteRegister(offset, value);
        return "ok";
    }

    @GetMapping("/api/modbus/registers/{offset}/{count}")
    public Optional<String> readString(@PathVariable Integer offset, @PathVariable Integer count) throws Exception {
        return Optional.ofNullable(modbusClient.readStringFromRegisters(offset, count));
    }

    @GetMapping("/api/modbus/registers/{offset}/dint")
    public Optional<Long> readDint(@PathVariable Integer offset) throws Exception {
        return Optional.ofNullable(modbusClient.readLongFromRegisters(offset));
    }

    @GetMapping("/api/modbus/registers/{offset}/high-byte")
    public Optional<String> readHighByte(@PathVariable Integer offset) throws ModbusException {
        return Optional.ofNullable(modbusClient.readHighByteFromRegister(offset)).map(modbusClient::toString);
    }

    @GetMapping("/api/modbus/registers/{offset}/low-byte")
    public Optional<String> readLowByte(@PathVariable Integer offset) throws ModbusException {
        return Optional.ofNullable(modbusClient.readLowByteFromRegister(offset)).map(modbusClient::toString);
    }

    @PostMapping("/api/modbus/registers/{offset}/{count}/poll")
    public String poll(@PathVariable Integer offset, @PathVariable Integer count,@RequestBody PollingCommand command) throws TimeoutException, ModbusException {
       return modbusClient.pollAndWaitForValues(command.getTimeout(),offset, count, command.getTriggerValues());
    }

}
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
class PollingCommand{
    private Integer timeout;
    private List<String> triggerValues;
}