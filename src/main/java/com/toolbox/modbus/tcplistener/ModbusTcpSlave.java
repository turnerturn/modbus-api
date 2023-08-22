package com.toolbox.modbus.tcplistener;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleDigitalOut;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.procimg.SimpleInputRegister;

import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A Modbus TCP Server demonstrating how to set up a listener and define a process image.
 *
 * Modbus Register Types:
 *  - Coils (Digital Outputs): Readable and writable single-bit values, usually representing physical outputs. Accessed using Modbus function codes 1 (read) and 5/15 (write).
 *  - Discrete Inputs (Digital Inputs): Read-only single-bit values, often representing the state of physical inputs like sensors. Accessed using Modbus function code 2 (read).
 *  - Holding Registers: Readable and writable 16-bit values, used for various data storage purposes. Accessed using Modbus function codes 3 (read) and 6/16 (write).
 *  - Input Registers: Read-only 16-bit values, often representing analog inputs. Accessed using Modbus function code 4 (read).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "modbus.dummy", name = "enabled", havingValue = "true")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//Add conditional on property to enable/disable this bean.
public class ModbusTcpSlave {
  private  ModbusTCPListener listener ;
 @Autowired
 private ModbusService modbusService;
    @Value("${modbus.address:127.0.0.1}")
    private String address;// "192.168.1.197"; // Modbus device IP address
    @Value("${modbus.port:502}")//Modbus.DEFAULT_PORT = 502
    private Integer port;
  @PostConstruct
  public void init() throws Exception{
            log.trace("init()");
                  // Create a listener with 10 threads
          listener = new ModbusTCPListener(10);

          // Prepare a process image
          SimpleProcessImage spi = new SimpleProcessImage();
          //see description of register in class level javadoc comments.
          spi.addDigitalOut(new SimpleDigitalOut(true)); // Coil for button 1
          spi.addDigitalOut(new SimpleDigitalOut(true)); // Coil for button 2
          spi.addDigitalOut(new SimpleDigitalOut(true)); // Coil for button 3 
          spi.addDigitalOut(new SimpleDigitalOut(true)); // Coil for button 4 
       int endingAddress = 50000;
       //Register[] registers = modbusService.stringToRegisterArray(variable1);
        
       // Add holding registers
        for (int i = 0; i <  endingAddress; i++) {
                spi.addRegister(new SimpleInputRegister((byte)0,(byte) 0)); // Adding 10 holding registers with initial value 0
        }

          // Set the image on the coupler
          ModbusCoupler.getReference().setProcessImage(spi);
          ModbusCoupler.getReference().setMaster(false);
          ModbusCoupler.getReference().setUnitID(15);
    
          listener.setAddress(InetAddress.getByName(this.address));
          listener.setPort(this.port);
          listener.start();
          log.info("Modbus TCP Listener started... Address: {} Port: {}",this.address,this.port);
  }
  @PreDestroy
  private void destory(){
    log.trace("destroy()");
        stopListener();
  }
  private void stopListener(){
        log.trace("stopListener()");
        try{
                listener.stop();
        }catch(Exception e){
                log.error("Failed to stop Modbus TCP Listener. ",e);
        }
  }
}
