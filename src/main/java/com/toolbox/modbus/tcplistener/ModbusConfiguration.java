
package com.toolbox.modbus.tcplistener;
import org.apache.catalina.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@Slf4j
@Configuration
@PropertySource(value = "classpath:application.yml")
@ConfigurationProperties(prefix = "modbus")
public class ModbusConfiguration {
   private ModbusSlaveDto slave;
    @Autowired
    private TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledJob;
@Autowired
private ModbusService modbusService;
    @PostConstruct
    public void init() {
        log.info("ModbusConfiguration: {}", slave);
        for (Coil coil : slave.getCoils()) {
            Optional.ofNullable(coil.getListener()).ifPresent(l -> {
                log.info("Listener: {}", l);
                //TODO create a class ButtonClickEventListener as runnable job that will watch coil for button click event.
                //scheduled job will trigger resulting actions when event is detected.
                //coil will be set back to its idle position after the event is detected.
                scheduledJob = taskScheduler.scheduleAtFixedRate(() -> {
                    log.info("Running job at: " + new Date());
                    try {
                        if(modbusService.readCoil(coil.getAddress()) == Boolean.valueOf(l.getValue())) {
                            log.info("Event: {}",l.getEvent());
                            //toggle coil to its idle position
                            modbusService.writeCoil(1, coil.getAddress(), false);
                            //TOODO trigger actions
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }, Instant.now(), Duration.ofSeconds(l.getPollingInterval()));
            });
        }   
    }
}
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
class ModbusSlaveDto{
    private String address;
    private Integer port;
    private List<HoldingRegister> holdingRegisters;
    private List<Coil> coils;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
class HoldingRegister{
    private String name;
    private Integer address;
    private Integer count;
    private Optional<String> initialValue;

}
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString   
class Coil{
    private String name;
    private Integer address;
    private String initialValue;
    private Listener listener;
}
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
class Listener {
    private String event;
    private String value;
    private Integer pollingInterval;
}