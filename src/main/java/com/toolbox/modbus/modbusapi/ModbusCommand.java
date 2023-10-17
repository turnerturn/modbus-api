package com.toolbox.modbus.modbusapi;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.wimpi.modbus.procimg.Register;

@AllArgsConstructor
@Builder
public class ModbusCommand {
    private ModbusCommandType commandType;
    private Integer registerOffset;
    private Integer registerCount;
    private List<Register> registers;
    private String data;
    public ModbusCommand () {}

    //getters and setters
    public ModbusCommandType getCommandType() {
        return commandType;
    }
    public void setCommandType(ModbusCommandType commandType) {
        this.commandType = commandType;
    }
    public Integer getRegisterOffset() {
        return registerOffset;
    }
    public void setRegisterOffset(Integer registerOffset) {
        this.registerOffset = registerOffset;
    }
    public Integer getRegisterCount() {
        return registerCount;
    }
    public void setRegisterCount(Integer registerCount) {
        this.registerCount = registerCount;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public List<Register> getRegisters() {
        return registers;
    }
    public void setRegisters(List<Register> registers) {
        this.registers = registers;
    }
    @Override
    public String toString() {
        return "ModbusCommand [commandType=" + commandType + ", registerOffset=" + registerOffset + ", registerCount="
                + registerCount + ", data=" + data + ", registers=" + registers + "]";
    }
}

