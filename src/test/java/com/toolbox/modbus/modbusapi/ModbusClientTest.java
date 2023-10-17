package com.toolbox.modbus.modbusapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

@SpringBootTest
public class ModbusClientTest {

    @Value("${modbus.host:localhost}")
    private String host;
    @Value("${modbus.port:9001}")
    private Integer port;
    @Autowired
    private ModbusClient modbusClient;

    @Test
    public void testWhenConvertingStringToByteWhereStringIsValid() {
        String testValue = "00001001";// 8 chars of 0 or 1.
        byte testValueAsByte = modbusClient.toByte(testValue);
        Assertions.assertEquals(testValue, modbusClient.toString(testValueAsByte));
    }

    @Test
    public void testWhenConvertingStringToByteWhereStringHasMoreThanEightCharacters() {
        String testValue = "000010011";// 9 chars of 0 or 1. Valid value is only 8 chars.
        boolean exceptionThrown = false;
        try {
            modbusClient.toByte(testValue);
        } catch (Exception e) {
            exceptionThrown = true;
            Assertions.assertTrue(e instanceof IllegalArgumentException,
                    "Illegal Argument Exception is expected to be thrown when converting string of " +testValue.length()+ " characters to byte.");
        }
        Assertions.assertTrue(exceptionThrown, "Exception should be thrown when converting invalid string to byte.");
    }
       @Test
    public void testWhenConvertingStringToByteWhereStringHasCharacterNotEqualToOneOrZero() {
        String testValue = "0000000A";// 8 chars. last char is "A"
        boolean exceptionThrown = false;
        try {
            modbusClient.toByte(testValue);
        } catch (Exception e) {
            exceptionThrown = true;
            Assertions.assertTrue(e instanceof IllegalArgumentException,
                    "Illegal Argument Exception is expected to be thrown when converting string to byte where string contains character not equal to 1 or 0.");
        }
        Assertions.assertTrue(exceptionThrown, "Exception should be thrown when converting invalid string to byte.");
    }

    @Test
    public void testWhenConvertingBetweenStringAndRegisters() throws Exception {
        String testValueWitLengthOfEvenValue = "foobar";// even length of characters
        Assertions.assertEquals((testValueWitLengthOfEvenValue.length() % 2), 0,
                "We require testValueWitLengthOfEvenValue to have length of even value for these unit tests.");
        Register[] testValueAsRegisters = modbusClient.toRegisters(testValueWitLengthOfEvenValue);
        Assertions.assertEquals(testValueWitLengthOfEvenValue, modbusClient.toString(testValueAsRegisters));

        String testValueWitLengthOfOddValue = testValueWitLengthOfEvenValue + "1";// odd length of characters
        Assertions.assertTrue((testValueWitLengthOfOddValue.length() % 2 > 0),
                "We require testValueWitLengthOfOddValue to have length of odd value for these unit tests.");
        testValueAsRegisters = modbusClient.toRegisters(testValueWitLengthOfOddValue);
        Assertions.assertEquals(testValueWitLengthOfOddValue + " ", modbusClient.toString(testValueAsRegisters));

    }

    @Test
    public void testWhenConvertingBetweenLongAndRegisters() throws Exception {
        Long testValue = 12341214L;// even length of characters
        Register[] testValueAsRegisters = modbusClient.toRegisters(testValue);
        Assertions.assertEquals(testValue, modbusClient.toLong(testValueAsRegisters));
    }

    @Test
    public void testWhenReadingAndWritingStringRegistersWithEvenLengthOfCharacters() throws Exception {
        String expectedValue = "foobar";
        Assertions.assertEquals(0, expectedValue.length() % 2, "String length must be even");
        modbusClient.writeRegisters(1, expectedValue);
        String readValue = modbusClient.readStringFromRegisters(1, expectedValue.length() / 2);
        Assertions.assertEquals(expectedValue, readValue, "Expected value was not read from registers");
    }

    @Test
    public void testWhenReadingAndWritingStringRegistersWithOddLengthOfCharacters() throws Exception {
        String testValue = "foobar" + "1";
        Assertions.assertTrue((testValue.length() % 2) > 0, "String length must be odd");
        Register[] registersFromTestValue = modbusClient.toRegisters(testValue);
        Register lastRegisterFromTestValue = registersFromTestValue[registersFromTestValue.length - 1];
        // We expect This low byte to equal int of 32 when dealing with string of odd
        // length characters.
        // 2 chars per register. second char of register is assigned to lowByte. In
        // string of odd length, the low byte of last register should equal int 32 bc
        // this low byte should not be used.
        Assertions.assertEquals(32, ModbusUtil.lowByte(lastRegisterFromTestValue.getValue()),
                "expected 32 in low byte of last register of testValueRegisters");

        modbusClient.writeRegisters(1, testValue);
        Register[] registersReadFromModbus = modbusClient.readRegisters(1, registersFromTestValue.length);
        Register lastRegisterReadFromModbus = registersReadFromModbus[registersReadFromModbus.length - 1];

        // We expect This low byte to equal int of 32 when dealing with string of odd
        // length characters.
        // 2 chars per register. second char of register is assigned to lowByte. In
        // string of odd length, the low byte of last register should equal int 32 bc
        // this low byte should not be used.
        Assertions.assertEquals(32, ModbusUtil.lowByte(lastRegisterReadFromModbus.getValue()),
                "expected 32 in low byte of last registe of lastRegisterReadFromModbus.");

    }

    @Test
    public void givenModbusCommandWithHighByteDataTypeWhenDataIsValidThenHighByteIsWrittenToRegisterAndLowByteIsPreserved() {
        String testValue = "00101100";
        // regex where string only contains 0 or 1 and only contains 8 characters.
        String regex = "[01]{8}";
        Assertions.assertTrue(testValue.matches(regex));
    }

    @Test
    public void givenModbusCommandWithLowByteDataTypeWhenDataIsValidThenLowByteIsWrittenToRegisterAndHighByteIsPreserved() {
        String testValue = "00101100";
        // regex where string only contains 0 or 1 and only contains 8 characters.
        String regex = "[01]{8}";
        Assertions.assertTrue(testValue.matches(regex));
    }

    @Test
    public void whenWritingHighByteToRegisterThenLowByteIsPersistedAndHighByteIsUpdatedWithNewValue() throws Exception {
        byte initialHighByte =  modbusClient.toByte("00000000");
        byte expectedHighByte = modbusClient.toByte("11111111");
        byte expectedLowByte = modbusClient.toByte("00000000");
        Register[] registers = {new SimpleRegister(initialHighByte, expectedLowByte)};
        modbusClient.writeRegisters(1, registers);

        modbusClient.writeHighByteRegister(1, expectedHighByte);
        Register registerAfterWrite = modbusClient.readRegisters(1, 1)[0];

        Assertions.assertEquals(expectedHighByte, ModbusUtil.hiByte(registerAfterWrite.getValue()));
        Assertions.assertEquals(expectedLowByte, ModbusUtil.lowByte(registerAfterWrite.getValue()));
    }

    @Test
    public void whenWritingLowByteToRegisterThenHighByteIsPersistedAndLowByteIsUpdatedWithNewValue() throws Exception {
        byte initialLowByte =  modbusClient.toByte("00000000");
        byte expectedLowByte = modbusClient.toByte("11111111");
        byte expectedHighByte = modbusClient.toByte("00000000");
        Register[] registers = {new SimpleRegister(expectedHighByte, initialLowByte)};
        modbusClient.writeRegisters(1, registers);

        modbusClient.writeLowByteRegister(1, expectedLowByte);
        Register registerAfterWrite = modbusClient.readRegisters(1, 1)[0];

        Assertions.assertEquals(expectedHighByte, ModbusUtil.hiByte(registerAfterWrite.getValue()));
        Assertions.assertEquals(expectedLowByte, ModbusUtil.lowByte(registerAfterWrite.getValue()));
    }

}