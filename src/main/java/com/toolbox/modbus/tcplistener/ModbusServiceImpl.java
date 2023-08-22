package com.toolbox.modbus.tcplistener;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

//TODO confitional bean on autocloseable modbus tcp master vs. a local dummy modbus master. (local dummy should require minimal external dependencies.)

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
    private String ipAddress = "192.168.1.197"; // Modbus device IP address
    private int port = 9000;// Modbus.DEFAULT_PORT; // Modbus default port 502
    private static final int BUTTON_1_COIL_ADDRESS = 0;
    private static final int BUTTON_2_COIL_ADDRESS = 0;

    public ModbusServiceImpl() {
    }

    private void assertValidRegisterGroup(int startingAddress, int endingAddress) throws IllegalArgumentException {
        if (endingAddress < startingAddress) {
            throw new IllegalArgumentException(
                    "The ending address must be greater than or equal to the starting address.");
        }
    }

    private void assertRegisterGroupCapacity(Register[] registers, int startingAddress, int endingAddress)
            throws IllegalArgumentException {
        if (registers.length > (endingAddress - startingAddress)) {
            throw new IllegalArgumentException("The value is too long to fit in the allotted registers.");
        }
    }

    private byte[] appendNullTerminatorWhenArrayLengthIsOdd(byte[] original) {
        // Check if the length of the original array is odd
        if (original.length % 2 != 0) {
            // Create a new array with one extra element
            byte[] newArray = new byte[original.length + 1];

            // Copy the original data into the new array
            System.arraycopy(original, 0, newArray, 0, original.length);

            // adding null terminator to last byte of new even length array
            newArray[newArray.length - 1] = (byte) 0;
            return newArray;
        }
        // If the original array length is not odd, return it as-is
        return original;
    }
    public boolean readCoil(int coilAddress) throws Exception {
        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(ipAddress, port)) {
            master.connect();
            return master.readCoils(coilAddress, 1).getBit(0);
        }
    }
    public boolean writeCoil(int coilAddress, boolean value) throws Exception {
        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(ipAddress, port)) {
            master.connect();
            master.writeCoil(coilAddress, 1,value);
            return readCoil(coilAddress);        
        }
    }


    public String readRegisters(int startingAddress, int endingAddress) throws Exception {
        assertValidRegisterGroup(startingAddress, endingAddress);

        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(ipAddress, port)) {
            master.connect();
            int registerCount = endingAddress - startingAddress;
            Register[] registers = master.readMultipleRegisters(startingAddress, registerCount);

            ByteBuffer byteBuffer = ByteBuffer.allocate(registers.length * 2);
            for (int i = 0; i < registerCount; i++) {
                SimpleRegister register = new SimpleRegister(registers[i].getValue());
                byteBuffer.putShort(ModbusUtil.registerToShort(register.toBytes()));
            }

            String result = new String(byteBuffer.array(), StandardCharsets.US_ASCII);
            log.info("String was retreived from modbus.  Value: {}", result);
            return result;
        }
    }

    public String writeRegisters(int startingAddress, int endingAddress, String value) throws Exception {
        Objects.requireNonNull(value, "value must not be null");
        assertValidRegisterGroup(startingAddress, endingAddress);

        try (AutoCloseableModbusTcpMaster master = new AutoCloseableModbusTcpMaster(ipAddress, port)) {
            master.connect();
            Register[] registers = stringToRegisterArray(value);
            assertRegisterGroupCapacity(registers, startingAddress, endingAddress);
            //TODO append null terminators to remaining space between endingAddress and startingAddress;
            // write the new value to the allotted group of registers
            master.writeMultipleRegisters(startingAddress, registers);
            log.info("Saved string to Modbus. Value: {}, StartingAddress: {}, EndingAddress: {}", value,
                    startingAddress, endingAddress);
                    return readRegisters(startingAddress, endingAddress);
        }

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
        // This avoids an index out of range exception when the string length is odd.
        bytes = appendNullTerminatorWhenArrayLengthIsOdd(bytes);
        Register[] registers = new Register[bytes.length / 2];
        for (int i = 0; i < (registers.length - 1); i++) {
            int highByte = bytes[i * 2] & 0xFF;
            int lowByte = bytes[i * 2 + 1] & 0xFF;
            registers[i] = new SimpleRegister(highByte << 8 | lowByte);
        }
        return registers;
    }

    public class AutoCloseableModbusTcpMaster extends ModbusTCPMaster implements AutoCloseable {
        public AutoCloseableModbusTcpMaster(String addr, int port) {
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

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
class RegisterDto {
    private int startingAddress;
    private int endingAddress;
    private String value;
}