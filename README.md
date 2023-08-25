# modbus-api
Provides api endpoints to interface applications with a given modbus slave.  

  - name: "button2"
    type: "coil"
    offset: 1
    count: 1
    description: "Coil to represent button1.  0 = not pressed, 1 = pressed"
    eventListeners:
      - name: "button2_clicked"
        when: "1"
        description: "button2 clicked"
  - name: "StringVariable"
    type: "holding"
    offset: 1
    count: 8
    dataType: "Text (char[16])"
    description: "Variable to save string value."
    eventListeners:
       - name: "StringVariable_Acked"
         when: "ack"
         description: "string variable was ackwoledged"