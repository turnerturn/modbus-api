package com.toolbox.modbus.modbusapi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class DummyModbusSlave {

    // "192.168.1.197"; // Modbus device IP address
    @Value("${modbus.host:127.0.0.1}")
    private String host;
    // Modbus.DEFAULT_PORT = 502
    @Value("${modbus.port:9001}")
    private Integer port;
    public void init() throws UnknownHostException{
        // Create a process image for the slave
        SimpleProcessImage spi = new SimpleProcessImage();
        for(int i = 0; i < 40001;i++){
            spi.addRegister(new SimpleRegister((byte) 0, (byte) 0));
        }

        // Initialize ModbusCoupler
        ModbusCoupler.getReference().setProcessImage(spi);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setUnitID(1);

        // Create the listener with default parameters
        ModbusTCPListener listener = new ModbusTCPListener(5); // 5 is the pool size

        listener.setAddress(InetAddress.getByName(host));
        listener.setPort(port);

        // Start the listener
        listener.start();
    }   
     @PostConstruct
    protected void postConstruct() throws UnknownHostException {
        init();
    }
}