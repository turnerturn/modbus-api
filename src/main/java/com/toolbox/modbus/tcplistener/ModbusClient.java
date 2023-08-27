package com.toolbox.modbus.tcplistener;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

@Slf4j
@Service
public class ModbusClient {
    @Autowired
    private Toolbox toolbox;

    private final Integer unitId;

    // "192.168.1.197"; // Modbus device IP address
    private final String address;
    // Modbus.DEFAULT_PORT = 502
    private final Integer port;

    private ModbusTCPMaster master;

    public ModbusClient(@Value("${modbus.slave.unitId:1}") Integer unitId,
            @Value("${modbus.slave.address:127.0.0.1}") String address,
            @Value("${modbus.slave.port:502}") Integer port) {
        this.unitId = unitId;
        this.address = address;
        this.port = port;
    }

    @PostConstruct
    protected void connect() throws Exception {
        this.master = new ModbusTCPMaster(address, port);
        this.master.connect();
    }

    @PreDestroy
    protected void disconnect() throws Exception {
        disconnect();
    }

    private void assertRegisterGroupCapacity(Register[] registers, Integer count)
            throws IllegalArgumentException {
        if (registers.length > count) {
            throw new IllegalArgumentException("The value is too long to fit in the allotted registers.");
        }
    }

    public boolean readCoil(Integer offset) throws Exception {
        return master.readCoils(offset, 1).getBit(0);
    }

    public boolean writeCoil(Integer offset, Boolean value) throws Exception {
        master.writeCoil(unitId, offset, value);
        return readCoil(offset);
    }

    public String readStringValueFromRegisters(Integer offset, Integer count) throws Exception {

        Register[] registers = master.readMultipleRegisters(offset, count);

        ByteBuffer byteBuffer = ByteBuffer.allocate(registers.length * 2);
        for (Integer i = 0; i < count; i++) {
            SimpleRegister register = new SimpleRegister(registers[i].getValue());
            byteBuffer.putShort(ModbusUtil.registerToShort(register.toBytes()));
        }

        String result = new String(byteBuffer.array(), StandardCharsets.US_ASCII);
        log.info("String was retreived from modbus.  Value: {}", result);
        return result;
    }

    public String writeStringValueToRegisters(Integer offset, Integer count, String value) throws Exception {

        Register[] registers = master.readMultipleRegisters(offset, count);
        assertRegisterGroupCapacity(registers, count);
        ByteBuffer byteBuffer = ByteBuffer.allocate(registers.length * 2);
        for (Integer i = 0; i < count; i++) {
            SimpleRegister register = new SimpleRegister(registers[i].getValue());
            byteBuffer.putShort(ModbusUtil.registerToShort(register.toBytes()));
        }

        String result = new String(byteBuffer.array(), StandardCharsets.US_ASCII);
        log.info("String was retreived from modbus.  Value: {}", result);
        return result;
    }

    public Register[] readRegisters(Integer offset, Integer count) throws Exception {
        return master.readMultipleRegisters(offset, count);
    }

    public void writeRegisters(Integer offset, Register[] registers) throws Exception {

        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(offset, registers);
        request.setUnitID(unitId);
        WriteMultipleRegistersResponse response = (WriteMultipleRegistersResponse) request.createResponse();
        log.info("Offset: {} WordCount: {}", response.getReference(), response.getWordCount());
    }

    public void writeRegisters(Integer offset, List<Register> registers) throws Exception {
        writeRegisters(offset, registers.toArray(new Register[registers.size()]));
    }

    /**
     * Converts a string value into an array of registers.
     * 
     * 
     * The stringToRegisterArray method takes a string value as input,
     * converts the string value into a byte array, and then creates an array of
     * registers consiting of the string's byte array.
     * 
     * You can then use this method to convert string values into a register array
     * as it may be needed within your Modbus server code.
     * 
     * Keep in mind that the SimpleRegister class uses a 16-bit integer value, so
     * the string should represent an integer value that fits within that range (0
     * to 65535). If the value falls outside this range, you'll need to handle that
     * condition as well.
     * 
     * @param value the string value to be converted
     * @return a holding register array representing the string value
     */
    public Register[] stringToRegisterArray(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);

        // This avoids an index out of range exception when the bytes length is odd.
        // Check if the length of the original array is odd
        int arraySize = (bytes.length % 2 != 0) ? bytes.length / 2 + 1 : bytes.length / 2;

        Register[] registers = new Register[arraySize];

        for (Integer i = 0; i < (registers.length); i++) {
            Integer highByte = bytes[i * 2] & 0xFF;
            // 0x20 is the byte for an empty space. We use this when wwe are handling a byte
            // array of odd length.
            Integer lowByte = ((bytes.length > (i * 2 + 1)) ? bytes[i * 2 + 1] & 0xFF : 0x20);
            registers[i] = new SimpleRegister(highByte << 8 | lowByte);
        }

        return registers;
    }

    public class AutoCloseableModbusTcpMaster extends ModbusTCPMaster implements AutoCloseable {
        public AutoCloseableModbusTcpMaster(String addr, Integer port) {
            super(addr, port);
        }

        @Override
        public void close() {
            try {
                disconnect();
            } catch (Exception e) {
                log.warn("Failed to disconnect from modbus.", e);
            }
        }
    }
}
