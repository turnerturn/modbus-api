package com.toolbox.modbus.tcplistener;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleDigitalOut;
import net.wimpi.modbus.procimg.SimpleInputRegister;

public class ModbusTcpListener {

  public static void main(String[] args) {
    try {
      // Prepare a process image
      SimpleProcessImage spi = new SimpleProcessImage();
          // Coils are represented with the DigitalOut registers.
        //Add a coil for each of our interface buttons.  
        //This coil should be set to true when the associated button is clicked on our interface.
       //This coil should be set to false after the button event is acknowledged by our modbus master.
        spi.addDigitalOut(new SimpleDigitalOut(true));
        //DigitalIn
      spi.addDigitalIn(new SimpleDigitalIn(false));
      spi.addRegister(new SimpleRegister(251));
      spi.addInputRegister(new SimpleInputRegister(45));
    // Add a coil register

      // Set the image on the coupler
      ModbusCoupler.getReference().setProcessImage(spi);
      ModbusCoupler.getReference().setMaster(false);
      ModbusCoupler.getReference().setUnitID(15);

      // Create a listener with 3 threads
      ModbusTCPListener listener = new ModbusTCPListener(3);
      listener.setPort(Modbus.DEFAULT_PORT);
      listener.start();

      System.out.println("Modbus TCP Listener started...");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
