package com.toolbox.modbus.tcplistener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.toolbox.modbus.tcplistener.ModbusServiceImpl.RegisterDto;

@RestController
public class ModbusController {
    @Autowired
    private ModbusService modbusService;

    @GetMapping("/api/modbus/{id}/registers/{offset}/{endingAddress}")
    public RegisterDto readRegisters(@PathVariable("id") Integer id,
            @PathVariable("offset") Integer offset,
            @PathVariable("endingAddress") Integer endingAddress) throws Exception {
        return modbusService.readRegisters(offset, endingAddress);
    }

    @GetMapping("/api/modbus/{id}/coils/{offset}")
    public Boolean readCoil(@PathVariable("id") Integer id, @PathVariable("offset") Integer offset)
            throws Exception {
        return modbusService.readCoil(offset);
    }

    @PostMapping("/api/modbus/{id}/coils/{offset}")
    public String writeCoil(@PathVariable("id") Integer id, @PathVariable("offset") Integer offset,
            @RequestBody Boolean value) throws Exception {
        modbusService.writeCoil(id, offset, value);
        return "success";
    }

    @PostMapping("/api/modbus/{id}/registers")
    public String writeRegisterGroups(@PathVariable("id") Integer id, @RequestBody List<RegisterDto> registerGroups)
            throws Exception {
        modbusService.writeRegisterGroups(id, registerGroups);
        return "success";
    }

    @PostMapping("/api/modbus/{id}/registers/{offset}/{count}")
    public String writeValueToRegisterGroup(@PathVariable("id") Integer id, @PathVariable("offset") Integer offset,
            @PathVariable("count") Integer count, @RequestBody String value) throws Exception {
        modbusService.writeValueToRegisterGroup(id, offset, count, value);
        return "success";
    }
}
// {offset,count,expression}
/**
 * ConsoleMessageDecorator
 * variables.forEach(v -> {
 * String scrapedValue = xmlScraper.scrape(xml,v.getExpression());
 * var consoleMessage = new ConsoleMessage(...,scrapedValue);
 * message.getConsoleMessages().getConsoleMessage().add(consoleMessage);
 */

// InitializeTouchscreenContext writes each of the console messages to their
// respective register.
// WaitConsoleButtonClicks waits for next button event.
// ProcessNextPageOfOptions
// ProcessPreviousPageOfOptions
// ProcessAbortEvent
// ProcessManualEntryEvent