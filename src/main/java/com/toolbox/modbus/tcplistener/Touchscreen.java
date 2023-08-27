package com.toolbox.modbus.tcplistener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Slf4j
@Component
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "touchscreen")
public class Touchscreen {
    @Autowired
    private Toolbox toolbox;
    // unitId of the modbus slave device
    private Integer unitId;
   // address of the touchscreen's modbus slave device
    private String address;
       // port of the touchscreen's modbus slave device
    private Integer port;
    private Integer pageSize;
    private Integer pageCount;
    private List<Variable> variables;
    private List<Button> buttons;
 
       @PostConstruct
       public void postConstruct(){
           log.info("Loaded touchscreen configuration from application.yml");
           log.info("Buttons: {}", toolbox.listToCsv(buttons));
           log.info("Variables: {}", toolbox.listToCsv(variables));
       }
}

/**
 * A variable is a logical representation of variables we track on the touchscreen.  
 * The touchscreen saves these string values to a sequence of modbus registers. 
 * Technical Details on Modbus Memory:
 * - Each register is 16 bits or 2 bytes. 
 * - Our string variable value is encoded using ASCII and each character is represented by one byte (8 bits). 
 * - To store a string of 16 characters, you would need 16 bytes.
 *  i.e. RegisterCount = stringValue.getBytes().length / 2BytesPerRegister
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class Variable{
    private String name;
    private Integer address; //modbus holding input register's starting address.
   //number of registers to read from starting address. 
   //A string value will be truncuated to fit in this alotted space of registers.   When truncuated, the last 3 characters of the string will be replaced with "...".
    private Integer count; 
    private Boolean persist; //whether or not to persist the value between each authenticated session at the touchscreen.
    private String value;
    private String expression;

    public boolean isPersisted(){
        return persist != null && persist;
    }
}

/**
 * A button is a logical representation of buttons we interface with on the touchscreen.  
 * Each button is associated with a coil register in the modbus.  
 * The touchscreen indicates a click event by setting the coil value to 1.  
 * Our application will read the coil value and set it back to 0 to indicate that we have handled the click event.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class Button{
    private String name;
    private Integer address; //modbus coil register's address.
    //The button type to help guide our business logic downstream of handling click events.
    private ButtonType type;
}
enum ButtonType {
        DONE_BUTTON, ABORT_BUTTON, MANUAL_ENTRY_BUTTON, SAVE_BUTTON, NEXT_OPTIONS_PAGE_BUTTON, PREVIOUS_OPTIONS_PAGE_BUTTON,
        DETAILS_PAGE_1_BUTTON,DETAILS_PAGE_2_BUTTON,DETAILS_PAGE_3_BUTTON,DETAILS_PAGE_4_BUTTON;
}
