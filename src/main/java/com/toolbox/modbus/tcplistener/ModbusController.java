package com.toolbox.modbus.tcplistener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import  com.toolbox.modbus.tcplistener.ModbusServiceImpl.RegisterDto;
@RestController
public class ModbusController {
    @Autowired
    private ModbusService modbusService;

    @GetMapping("/api/modbus/{id}/registers/{startingAddress}/{endingAddress}")
    public RegisterDto readRegisters(@PathVariable("id") Integer id,
            @PathVariable("startingAddress") Integer startingAddress,
            @PathVariable("endingAddress") Integer endingAddress) throws Exception {
        return modbusService.readRegisters(startingAddress, endingAddress);
    }

    @GetMapping("/api/modbus/{id}/coils/{startingAddress}")
    public boolean readCoil(@PathVariable("id") Integer id, @PathVariable("startingAddress") Integer startingAddress)
            throws Exception {
        return modbusService.readCoil(startingAddress);
    }

    @PostMapping("/api/modbus/{id}/coils")
    public boolean writeCoil(@PathVariable("id") Integer id,  @RequestBody RegisterDto  dto) throws Exception {
        return modbusService.writeCoil(id,dto.getStartingAddress(),Boolean.parseBoolean(dto.getValue()));
    }
    @PostMapping("/api/modbus/{id}/registers")
    public RegisterDto writeRegisters(@PathVariable("id") Integer id,  @RequestBody RegisterDto dto) throws Exception {
        return modbusService.writeRegisters(id,dto);
    }
}
//{offset,count,expression}
/** ConsoleMessageDecorator
    variables.forEach(v -> {
        String scrapedValue = xmlScraper.scrape(xml,v.getExpression());
        var consoleMessage = new ConsoleMessage(...,scrapedValue);
        message.getConsoleMessages().getConsoleMessage().add(consoleMessage);
*/

//InitializeTouchscreenContext writes each of the console messages to their respective register.
//WaitConsoleButtonClicks waits for next button event.
//ProcessNextPageOfOptions
//ProcessPreviousPageOfOptions
//ProcessAbortEvent
//ProcessManualEntryEvent