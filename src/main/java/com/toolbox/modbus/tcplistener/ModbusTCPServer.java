package com.toolbox.modbus.tcplistener;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleDigitalOut;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.procimg.SimpleInputRegister;

import java.net.InetAddress;

import org.springframework.stereotype.Component;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
//Add conditional on property to enable/disable this bean.
public class ModbusTCPServer {
  private  ModbusTCPListener listener ;
  private String address = "127.0.0.1";
  private int port = 9000;//Modbus.DEFAULT_PORT; (default port = 502)
  public ModbusTCPServer(){

  }
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
          spi.addRegister(new SimpleRegister(251));      // Holding Register to save memory for string variable 1.
         // spi.addDigitalIn(new SimpleDigitalIn(false));  // Discrete Input
         // spi.addInputRegister(new SimpleInputRegister(45)); // Input Register  
    
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
