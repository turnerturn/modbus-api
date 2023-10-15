package com.toolbox.modbus.tcplistener;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

@Slf4j
@Component
public class ModbusClient {
    @Autowired
    private Toolbox toolbox;

    @Value("${modbus.unitId:1}")
    private Integer unitId;

    // "192.168.1.197"; // Modbus device IP ip
    @Value("${modbus.host:127.0.0.1}")
    private String host;
    // Modbus.DEFAULT_PORT = 502
    @Value("${modbus.port:9001}")
    private Integer port;

    public ModbusClient() {
    }

    public ModbusClient(Integer unitId, String host, Integer port) {
        this.unitId = unitId;
        this.host = host;
        this.port = port;
    }

    public Register[] readRegisters(Integer offset, Integer count) throws Exception {
        try (AutoCloseableModbusTcpMaster m = new AutoCloseableModbusTcpMaster(
                InetAddress.getByName(host).getHostName(), port)) {
            m.connect();
            return m.readMultipleRegisters(offset, count);
        }
    }

    public void writeRegisters(Integer offset, List<Register> registerList) throws Exception {
       
        try (AutoCloseableModbusTcpMaster m = new AutoCloseableModbusTcpMaster(
                InetAddress.getByName(host).getHostName(), port)) {
            m.connect();
            List<List<Register>> listOfChunkedRegisters = chunkRegisters(registerList, 125);
            for(List<Register> chunkedRegisterList : listOfChunkedRegisters){
                Register[] registers = chunkedRegisterList.toArray(new Register[chunkedRegisterList.size()]);
                m.writeMultipleRegisters(offset, registers);
                offset += registers.length;
            }
        }
    }

    /**
     * Converts a byte array to an array of SimpleRegister.
     *
     * @param byteArray The input byte array.
     * @return An array of SimpleRegister.
     */
    public Register[] toRegisterArray(byte[] bytes) {
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


/**
 * Converts an array of registers into a long value.  Dint datatype requires 2 registers per long value.
 * @param registers
 * @return
 * @throws Exception
 */
    public long registersToLong(Register[] registers) throws Exception {
        Register register1 = new SimpleRegister(registers[0].getValue());
        Register register2 = new SimpleRegister(registers[1].getValue());
      // Create a ByteBuffer to hold the two 16-bit registers
      ByteBuffer buffer = ByteBuffer.allocate(4);

      // Put the two registers into the ByteBuffer as two shorts
      buffer.putShort( register1.toShort());
      buffer.putShort(register2.toShort());

              // Flip the ByteBuffer to read it
              buffer.flip();
                // Retrieve the combined long value
              return buffer.getInt() & 0xFFFFFFFFL;
    }
/**
 * Converts a long value into an array of registers representing a 'dint' dataTyoe.  Dint datatype requires 2 registers per long value.
 * @param value
 * @return
 */
    public Register[] longToRegisters(Long value) {

        int register1 = (int) ((value >> 48) & 0xFFFF); // Most significant 16 bits
        int register2 = (int) ((value >> 32) & 0xFFFF); // Next 16 bits
        int register3 = (int) ((value >> 16) & 0xFFFF); // Next 16 bits
        int register4 = (int) (value & 0xFFFF); // Least significant 16 bits
        Register[] registers = { new SimpleRegister(register1), new SimpleRegister(register2),
                new SimpleRegister(register3), new SimpleRegister(register4) };
        return registers;
    }


/**
 * Converts a string value into a byte value.
 * @param input
 * @return
 */
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
/**
 * Converts a byte value into a string value.
 * @param inputByte
 * @return
 */
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
    protected Register[] stringToRegisterArray(String value) {
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
     * @param registers
     * @return
     * @throws Exception
     */
    public String registersArrayToString(Register[] registers) throws Exception {
        //log.trace("toString(...)");
        Objects.requireNonNull(registers, "registers is null");
        ByteBuffer byteBuffer = ByteBuffer.allocate(registers.length * 2);
        for (Register register : registers) {
            byteBuffer.putShort(ModbusUtil.registerToShort(new SimpleRegister(register.getValue()).toBytes()));
        }
        String result = new String(byteBuffer.array(), StandardCharsets.US_ASCII);
        //log.debug("Registers was converted to string.  Value: {}", result);
        return result;
    }

    /**
     * The chunkRegisters function takes a list of items and the desired chunk size as input.
     * It then iterates through the list and creates a new list for each chunk of items.
     * 
     * 
     * @param list      The list to be chunked
     * @param chunkSize The desired chunk size
     * @return An ArrayList containing all the chunked lists.
     */
    public List<List<Register>> chunkRegisters(List<Register> registers, int chunkSize) {
        List<List<Register>> chunkedList = new ArrayList<>();

        int totalSize = registers.size();

        for (int i = 0; i < totalSize; i += chunkSize) {
            int end = Math.min(i + chunkSize, totalSize);
            List<Register> chunk = registers.subList(i, end);
            chunkedList.add(new ArrayList<>(chunk));
        }

        return chunkedList;
    }
/**
 * This class is used to create a ModbusTCPMaster object that implements the AutoCloseable interface.
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