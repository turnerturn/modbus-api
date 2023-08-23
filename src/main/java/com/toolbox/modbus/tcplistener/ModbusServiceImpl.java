package com.toolbox.modbus.tcplistener;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

@Slf4j
@Getter
@Setter
@Service
public class ModbusServiceImpl implements ModbusService {

    /*
     * Read Modbus Registers
     * HTTP GET /api/modbus/{id}/{startingAddress}/{endingAddress}
     * Validates endingAddress >= startingAddress
     * 
     * Write Modbus Registers
     * HHTP POST /api/modbus/{id}/{startingAddress}/{endingAddress}
     * BODY: { value: "...."}
     * 
     * Validates endingAddress >= startingAddress
     * Validates value can be written into the capacity of registers
     */
    @Value("${modbus.address:127.0.0.1}")
    private String address;// "192.168.1.197"; // Modbus device IP address
    @Value("${modbus.port:502}") // Modbus.DEFAULT_PORT = 502
    private Integer port;

    public ModbusServiceImpl() {
    }

    private void assertRegisterGroupCapacity(Register[] registers, Integer count)
            throws IllegalArgumentException {
        if (registers.length > count) {
            throw new IllegalArgumentException("The value is too long to fit in the allotted registers.");
        }
    }

    private byte[] appendEmptySpaceWhenArrayLengthIsOdd(byte[] original) {
        // Check if the length of the original array is odd
        if (original.length % 2 != 0) {
            // Create a new array with one extra element
            byte[] newArray = new byte[original.length + 1];

            // Copy the original data into the new array
            System.arraycopy(original, 0, newArray, 0, original.length);

            // adding null terminator to last byte of new even length array
            newArray[newArray.length - 1] = 0x20;
            return newArray;
        }
        // If the original array length is not odd, return it as-is
        return original;
    }

    public boolean readCoil(Integer offset) throws Exception {
        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(address, port)) {
            master.connect();
            return master.readCoils(offset, 1).getBit(0);
        }
    }

    public boolean writeCoil(Integer unitId, Integer offset, Boolean value) throws Exception {
        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(address, port)) {
            master.connect();
            master.writeCoil(unitId, offset, value);
            return readCoil(offset);
        }
    }

    public String readHoldingRegistersValue(Integer offset, Integer count) throws Exception {
        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(address, port)) {
            master.connect();

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
    }

    public void writeHoldingRegistersValue(Integer unitId, Integer offset, Integer count,String value) throws Exception {
        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(address, port)) {
            master.connect();
            Objects.requireNonNull(offset, "startingAddress must not be null");
            Objects.requireNonNull(count, "value must not be null");

            Integer reference = offset; // Reference of the register to be written
            Register[] valueRegisters = stringToRegisterArray(value);
            // pad null terminator registers to the right so we can fill the given
            // allottment of registers.
            // This removes any old remnants of values that may have previously been
            // written.
            // registers = padRight(endingAddress - registers.length, registers,new
            // SimpleRegister((byte)0, (byte)0));

            assertRegisterGroupCapacity(valueRegisters,count);
            WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(offset, valueRegisters);
            request.setUnitID(unitId);
            WriteMultipleRegistersResponse response = (WriteMultipleRegistersResponse) request.createResponse();
            log.info("Offset: {} WordCount: {}", response.getReference(), response.getWordCount());

        }
    }

    public void uploadRegisters(Integer unitId, List<RegisterDto> registers) throws Exception {
        Objects.requireNonNull(registers, "registers must not be null");
        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(address, port)) {
            master.connect();
            for (RegisterDto dto : registers) {
                Objects.requireNonNull(dto.getOffset(), "startingAddress must not be null");
                Objects.requireNonNull(dto.getValue(), "value must not be null");

                Integer reference = dto.getOffset(); // Reference of the register to be written
                Register[] valueRegisters = stringToRegisterArray(dto.getValue());
                // pad null terminator registers to the right so we can fill the given
                // allottment of registers.
                // This removes any old remnants of values that may have previously been
                // written.
                // registers = padRight(endingAddress - registers.length, registers,new
                // SimpleRegister((byte)0, (byte)0));

                if (dto.getCount() != null) {
                    assertRegisterGroupCapacity(valueRegisters, dto.getCount());
                    // TODO append null terminators to remaining space between endingAddress and
                    // startingAddress;
                    // write the new value to the allotted group of registers
                }

                WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(reference, valueRegisters);
                request.setUnitID(unitId);
                WriteMultipleRegistersResponse response = (WriteMultipleRegistersResponse) request.createResponse();
                log.info("Offset: {} WordCount: {}", response.getReference(), response.getWordCount());
            }
        }
    }

    private boolean isCoilRegister(Integer offset) {
        return offset >= 0 && offset <= 9999;
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

    public Register[] padRight(int padding, Register[] registers, Register paddingValue) {
        int i = registers.length;
        registers = Arrays.copyOf(registers, registers.length + padding);
        // Pad the array to the right with the specified pad value.
        Arrays.fill(registers, i, padding, paddingValue);
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

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    static class RegisterDto {
        private Integer offset;
        private Integer count;
        private String value;
    }
}
