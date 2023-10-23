package com.toolbox.modbus.modbusapi;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

/**
 * The ModbusClient class is used to read and write data to a Modbus device.
 * 
 * @author mturner
 */
@Slf4j
@Component
public class ModbusClient {

    public static final int MAX_REGISTERS_PER_REQUEST = 124;
    @Value("${modbus.host:127.0.0.1}")
    private String host;
    // Modbus.DEFAULT_PORT = 502
    @Value("${modbus.port:9001}")
    private Integer port;
    @Autowired
    private Toolbox toolbox;

    public ModbusClient() {
    }

    public ModbusClient(String host, Integer port, Toolbox toolbox) {
        this.host = host;
        this.port = port;
        this.toolbox = toolbox;
    }

    public String pollAndWaitForValues(int timeout, Integer offset, Integer count, List<String> values)
            throws TimeoutException, ModbusException {
       return pollAndWaitForValues(timeout, offset, count, values.toArray(new String[values.size()]));
    }

    public String pollAndWaitForValues(int timeout, Integer offset, Integer count, String... values)
            throws TimeoutException, ModbusException {
        // defaults to 1 minute if null.
        LocalTime expiryTime = LocalTime.now(ZoneId.systemDefault())
                .plusSeconds(Optional.ofNullable(timeout).orElse(60));
        try (AutoCloseableModbusTcpMaster m = new AutoCloseableModbusTcpMaster(
                InetAddress.getByName(host).getHostName(), port)) {
            m.connect();
            while (true) {
                if (LocalTime.now(ZoneId.systemDefault()).isAfter(expiryTime)) {
                    throw new TimeoutException("Polling has timed out after " + timeout + " seconds.");
                }
                String readValue = Optional.ofNullable(toString(m.readMultipleRegisters(offset, count)))
                        .map(str -> str.trim()).orElse("");
                // true if we read a value that matches a value in our list of values to poll
                // for.
                if (values != null && Arrays.asList(values).stream().anyMatch(val -> val.equalsIgnoreCase(readValue))) {
                    return readValue;
                }
                // sleep our thread before we try again.
                sleep(1000);
            }
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new ModbusException("Failed to poll registers for string value. ", e);
        }
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.warn("Failed to sleep thread.", e);
        }
    }

    public void clearRegisters(Integer offset, Integer count) throws ModbusException {
        try (AutoCloseableModbusTcpMaster m = new AutoCloseableModbusTcpMaster(
                InetAddress.getByName(host).getHostName(), port)) {
            m.connect();
            List<Register> registers = new ArrayList<>();
            toolbox.fillList(registers, new SimpleRegister((byte) 0, (byte) 0), count);
            m.writeMultipleRegisters(offset, registers.toArray(new Register[registers.size()]));
        } catch (Exception e) {
            throw new ModbusException("Failed to clear registers. ", e);
        }
    }

    public Register[] readRegisters(Integer offset, Integer count) throws Exception {
        try (AutoCloseableModbusTcpMaster m = new AutoCloseableModbusTcpMaster(
                InetAddress.getByName(host).getHostName(), port)) {
            m.connect();
            return m.readMultipleRegisters(offset, count);
        }
    }

    public void writeRegisters(Integer offset, Register[] registers) throws Exception {
        try (AutoCloseableModbusTcpMaster m = new AutoCloseableModbusTcpMaster(
                InetAddress.getByName(host).getHostName(), port)) {
            m.connect();
            m.writeMultipleRegisters(offset, registers);
        }
    }

    public void writeRegisters(Integer offset, List<Register> registers) throws Exception {
        writeRegisters(offset, registers.toArray(new Register[registers.size()]));
    }

    public void writeRegisters(Integer offset, String value) throws Exception {
        writeRegisters(offset, toRegisters(value));
    }

    public void writeRegisters(Integer offset, Long value) throws Exception {
        writeRegisters(offset, toRegisters(value));
    }

    /**
     * Writes a value to the low byte of a Modbus register at the specified offset.
     * The high byte of this register will be persisted.
     * 
     * @param offset the offset of the register to write to
     * @param value  the value to write to the low byte of the register
     * @throws ModbusException if there is an error reading or writing the register
     */
    public void writeLowByteRegister(Integer offset, String value) throws ModbusException {
        writeLowByteRegister(offset, toByte(value));
    }

    /**
     * Writes the low byte of a 16-bit value to a Modbus register at the specified
     * offset.
     *
     * @param offset  the offset of the register to write to
     * @param lowByte the low byte of the 16-bit value to write
     * @throws ModbusException if there is an error writing to the register
     */
    public void writeLowByteRegister(Integer offset, byte lowByte) throws ModbusException {
        try {
            Register[] registers = readRegisters(offset, 1);
            byte highByte = ModbusUtil.hiByte(registers[0].getValue());
            // wait half a second before our next modbus request.
            sleep(500);
            writeRegisters(offset, Arrays.asList(new SimpleRegister(highByte, lowByte)));
        } catch (Exception e) {
            throw new ModbusException("Failed to write low byte to registers. ", e);
        }
    }

    /**
     * Writes the high byte of the given value to the register at the specified
     * offset.
     *
     * @param offset the offset of the register to write to
     * @param value  the value to extract the high byte from and write to the
     *               register
     * @throws ModbusException if there is an error writing to the register
     */
    public void writeHighByteRegister(Integer offset, String value) throws ModbusException {
        writeHighByteRegister(offset, toByte(value));
    }

    public void writeHighByteRegister(Integer offset, byte highByte) throws ModbusException {
        try {
            Register[] registers = readRegisters(offset, 1);
            byte lowByte = ModbusUtil.lowByte(registers[0].getValue());
            // wait half a second before our next modbus request.
            sleep(500);
            writeRegisters(offset, Arrays.asList(new SimpleRegister(highByte, lowByte)));
        } catch (Exception e) {
            throw new ModbusException("Failed to write high byte to registers. ", e);
        }
    }

    public String readStringFromRegisters(int registerOffset, int registerCount) throws ModbusException {
        try {
            int remainingRegisterCount = registerCount;

            StringBuilder sb = new StringBuilder();
            while (remainingRegisterCount > 0) {
                registerCount = (registerCount > 125) ? 125 : registerCount;
                remainingRegisterCount -= registerCount;
                sb.append(toString(readRegisters(registerOffset, registerCount)));
                registerOffset += registerCount;
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ModbusException("Failed to read string from registers. ", e);
        }
    }

    public Long readLongFromRegisters(int offset) throws ModbusException {
        try {
            Register[] registers = readRegisters(offset, 2);
            return toLong(registers);
        } catch (Exception e) {
            throw new ModbusException("Failed to read long from registers. ", e);
        }
    }

    public byte readLowByteFromRegister(int offset) throws ModbusException {
        try {
            Register[] registers = readRegisters(offset, 1);
            return ModbusUtil.lowByte(registers[0].getValue());
        } catch (Exception e) {
            throw new ModbusException("Failed to read low byte from register. ", e);
        }
    }

    public byte readHighByteFromRegister(int offset) throws ModbusException {
        try {
            Register[] registers = readRegisters(offset, 1);
            return ModbusUtil.hiByte(registers[0].getValue());
        } catch (Exception e) {
            throw new ModbusException("Failed to read high byte from register. ", e);
        }
    }

    protected void publishRegisterValueToDestination(String destination, String value) {
        // send jms message to destination with value as payload.
    }

    /**
     * Converts a byte array to an array of SimpleRegister.
     *
     * @param byteArray The input byte array.
     * @return An array of SimpleRegister.
     */
    public Register[] toRegisters(byte[] bytes) {
        // Calculate the length of the Register array
        int length = bytes.length / 2;

        // Initialize the SimpleRegister array
        SimpleRegister[] registerArray = new SimpleRegister[length];

        // Loop through the SimpleRegister array and populate it
        for (int i = 0; i < length; i++) {
            int value = ((bytes[i * 2] & 0xFF) << 8) | (bytes[i * 2 + 1] & 0xFF);
            registerArray[i] = new SimpleRegister(value);
        }

        return registerArray;
    }

    public long toLong(Register[] dintRegisters) {
        // Combine the two 16-bit registers into a 32-bit integer
        int combinedInt = (dintRegisters[0].getValue() << 16) | (dintRegisters[1].getValue() & 0xFFFF);

        // Convert the 32-bit integer to a long (if needed)
        long combinedLong = combinedInt & 0xFFFFFFFFL;

        return combinedLong;
    }

    /**
     * Converts a long value into an array of two Modbus registers.
     *
     * @param longValue the long value to convert
     * @return an array of two Modbus registers
     */
    public Register[] toRegisters(long longValue) {
        // Extract the upper 16 bits and lower 16 bits from the long value
        int upperBits = (int) (longValue >> 16);
        int lowerBits = (int) longValue;

        // Create an array to hold the two Modbus registers
        Register[] registers = { new SimpleRegister(upperBits), new SimpleRegister(lowerBits) };

        return registers;
    }

    /**
     * Converts a string value into a byte value.
     * 
     * @param input
     * @return
     */
    protected byte toByte(String input) throws IllegalArgumentException {
        String regex = "[01]{8}";
        Assert.isTrue(input.matches(regex),
                "String value of byte must consist of 8 characters, each of which is either '0' or '1'");
        byte result = 0;

        for (int i = 0; i < 8; i++) {
            char c = input.charAt(i);
            if (c == '1') {
                result |= (1 << (i));
            }
        }
        return result;
    }

    /**
     * Converts a byte value into a string value.
     * 
     * @param b
     * @return
     */
    public String toString(byte bits) {
        StringBuilder stringBuilder = new StringBuilder(8);

        // Iterate through each bit in the byte
        for (int i = 0; i < 8; i++) {
            // Use bitwise AND operation to check the value of the bit
            byte bitValue = (byte) ((bits >> i) & 1);

            // Append '0' or '1' to the StringBuilder
            stringBuilder.append(bitValue);
        }

        return stringBuilder.toString();
    }

    /**
     * Converts a string value into an array of registers.
     * 
     * 
     * The toRegisters method takes a string value as input,
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
    protected Register[] toRegisters(String value) {
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

    /**
     * Converts an array of registers into a string value.
     * 
     * @param registers
     * @return
     * @throws Exception
     */
    public String toString(Register[] registers) throws Exception {
        Objects.requireNonNull(registers, "registers is null");
        ByteBuffer byteBuffer = ByteBuffer.allocate(registers.length * 2);
        for (Register register : registers) {
            byteBuffer.putShort(ModbusUtil.registerToShort(new SimpleRegister(register.getValue()).toBytes()));
        }
        byte[] bytes = toolbox.removeNullBytes(byteBuffer.array());
        // replace all nul bytes of our string with empty space. We can frequently have
        // nul bytes trailing the end of a list of registers.
        return new String(bytes, StandardCharsets.US_ASCII).toString();
    }

    /**
     * This class is used to create a ModbusTCPMaster object that implements the
     * AutoCloseable interface.
     * 
     * @author mturner
     */
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