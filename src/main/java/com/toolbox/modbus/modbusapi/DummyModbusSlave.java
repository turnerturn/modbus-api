package com.toolbox.modbus.modbusapi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

@Component
public class DummyModbusSlave {

    //
    private String host = "localhost";
    //
    private Integer port = 9002;
    // Create the listener with default parameters
    private ModbusTCPListener listener = new ModbusTCPListener(5); // 5 is the pool size

    public DummyModbusSlave(@Value("${modbus.host:127.0.0.1}") String host, @Value("${modbus.port:9002}")Integer port) {
        this.host = host;
        this.port = port;
    }
    
    public void init() throws UnknownHostException {
        // Create a process image for the slave
        SimpleProcessImage spi = new SimpleProcessImage();
        for (int i = 0; i < 40001; i++) {
            spi.addRegister(new SimpleRegister((byte) 0, (byte) 0));
        }

        // Initialize ModbusCoupler
        ModbusCoupler.getReference().setProcessImage(spi);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setUnitID(1);

        listener.setAddress(InetAddress.getByName(host));
        listener.setPort(port);

        // Start the listener
        listener.start();
    }

    @PostConstruct
    protected void postConstruct() throws UnknownHostException {
        init();
    }

    public void stop() {
        listener.stop();
    }
}