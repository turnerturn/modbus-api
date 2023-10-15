package com.toolbox.modbus.tcplistener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    private List<ModbusCommandHandler> handlers;
    @Autowired
    private ModbusApiClient modbusApiClient;

    @PostMapping("/api/modbus/registers/read")
    public ModbusCommandResponse read(@RequestBody ModbusCommandRequest dto) throws Exception {
        ModbusCommand command = ModbusCommand.builder().registerOffset(dto.getOffset()).registerCount(dto.getCount())
                .data(dto.getData()).build();
        if ("string".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.READ_STRING_FROM_REGISTERS);
        }
        if ("dint".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.READ_DINT_FROM_REGISTERS);
        }
        if ("low-byte".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.READ_LOW_BYTE_FROM_REGISTERS);
        }
        if ("high-byte".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.READ_HIGH_BYTE_FROM_REGISTERS);
        }
        return handlers.stream().filter(h -> h.isMine(command)).findFirst().map(h -> h.execute(command))
                .orElseThrow(() -> new Exception("Failed to read modbus registers."));
    }

    @PostMapping("/api/modbus/registers/clear")
    public ModbusCommandResponse clear(@RequestBody ModbusCommandRequest dto) throws Exception {
        ModbusCommand command = ModbusCommand.builder().registerOffset(dto.getOffset()).registerCount(dto.getCount())
                .data(dto.getData()).build();
        command.setCommandType(ModbusCommandType.CLEAR_MEMORY_FROM_REGISTERS);
        return handlers.stream().filter(h -> h.isMine(command)).findFirst().map(h -> h.execute(command))
                .orElseThrow(() -> new Exception("Failed to read modbus registers."));
    }

    @PostMapping("/api/modbus/registers/write")
    public ModbusCommandResponse write(@RequestBody ModbusCommandRequest dto) throws Exception {
        ModbusCommand command = ModbusCommand.builder().registerOffset(dto.getOffset()).registerCount(dto.getCount())
                .data(dto.getData()).build();
        if ("string".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.WRITE_STRING_TO_REGISTERS);
        }
        if ("dint".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.WRITE_DINT_TO_REGISTERS);
        }
        if ("low-byte".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.WRITE_LOW_BYTE_TO_REGISTERS);
        }
        if ("high-byte".equalsIgnoreCase(dto.getDataType())) {
            command.setCommandType(ModbusCommandType.WRITE_HIGH_BYTE_TO_REGISTERS);
        }
        return handlers.stream().filter(h -> h.isMine(command)).findFirst().map(h -> h.execute(command))
                .orElseThrow(() -> new Exception("Failed to write modbus registers."));
    }

}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
class ModbusCommandRequest {
    private Integer offset;
    private Integer count;
    private String data;
    private String dataType;
}