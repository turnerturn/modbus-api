package com.toolbox.modbus.tcplistener;

import java.util.List;

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

    @GetMapping("/api/modbus/{id}/registers/{offset}/{count}")
    public String readValueFromRegisters(@PathVariable("id") Integer id,
            @PathVariable("offset") Integer offset,
            @PathVariable("count") Integer count) throws Exception {
        return modbusService.readValueFromRegisters(offset, count);
    }
    
    @PostMapping("/api/modbus/{id}/registers")
    public String writeRegisters(@PathVariable("id") Integer id,  @RequestBody List<RegisterDto> registers) throws Exception {
         modbusService.writeRegisters(id,registers);
         return "success";
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