package com.toolbox.modbus.tcplistener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

@Component
@Slf4j
@RequiredArgsConstructor
public class TouchscreenAssistant {

    private final ModbusClient modbusClient;
    @Autowired
    private Touchscreen touchscreen;
    @Autowired
    private Toolbox toolbox;
    private Integer currentDetailsPageIndex;
   private Integer currentOptionsPage;

    @PostConstruct
    private void init(){
        currentOptionsPage = getFirstOptionsPageIndex();
        currentDetailsPageIndex = 1;
    }
    public void process(String xmlMessage) throws Exception {
        clearSessionVariables();
        // TODO extract parent element from xml
        // TODO Save xml object to memory
        //  assign page number attribute to xml elements
        assignPageNumberAttributes();

        // extract variables from elements where page > 0
        decorateVariablesWithMessage();
        writeAllVariables();
    }

 
    public Integer getCurrentOptionsPageIndex() {
        return currentOptionsPage;
    }

    public Integer getFirstOptionsPageIndex() {
        return 1;
    }

    public Integer getLastOptionsPageIndex() {
        return touchscreen.getPageCount();
    }

    public Integer getNextOptionsPageIndex() {
        int currentPageIndex = getCurrentOptionsPageIndex();
        if (currentPageIndex + 1 > getLastOptionsPageIndex()) {
            return getFirstOptionsPageIndex();
        } else {
            return currentPageIndex + 1;
        }
    }

    public Integer getPreviousOptionsPageIndex() {
        int currentPageIndex = getCurrentOptionsPageIndex();
        if (currentPageIndex - 1 < getFirstOptionsPageIndex()) {
            return getLastOptionsPageIndex();
        } else {
            return currentPageIndex - 1;
        }
    }
    private void assignPageNumberAttributes(){

    }
    private void decorateVariablesWithMessage(){

    }
    private void decorateMessageWithVariables(){

    }
    public void writeAllVariables() {
        touchscreen.getVariables().stream().forEach(this::writeVariable);
    }
    public List<Variable> readAllVariables() {
        List<Variable> variables = new ArrayList<>();
        touchscreen.getVariables().stream().map(this::readVariable).forEach(variables::add);
        ;
        return variables;
    }
    public void writeVariable(Variable variable) {
        try {
            modbusClient.writeStringValueToRegisters(variable.getAddress(), variable.getCount(),variable.getValue());
        } catch (Exception e) {
            log.warn("Failed to write variable.  Variable: {}", variable, e);
        }
    }
    public Variable readVariable(Variable variable) {
        try {
            String value = modbusClient.readStringValueFromRegisters(variable.getAddress(), variable.getCount());
            return new Variable(variable.getName(), variable.getAddress(), variable.getCount(), variable.isPersisted(),
                    value, variable.getExpression());
        } catch (Exception e) {
            log.warn("Failed to read variable.  Variable: {}", variable, e);
        }
        return new Variable(variable.getName(), variable.getAddress(), variable.getCount(), variable.isPersisted(),
                null, variable.getExpression());
    }

    public void clearSessionVariables() throws Exception {
        List<Exception> exceptions = new ArrayList<>();
        // We do not clear variables where variable.persist = true.
        List<Variable> nonPersistedVariables = touchscreen.getVariables().stream().filter(v -> !v.isPersisted())
                .collect(Collectors.toList());
        for (Variable variable : nonPersistedVariables) {
            try {
                clearVariable(variable);
            } catch (Exception e) {
                log.warn("Failed to clear session variable.  Variable: {}", variable, e);
                exceptions.add(e);
            }
        }
        if (exceptions.size() > 0) {
            throw new Exception("Failed to clear " + exceptions.size() + " session variables.  See logged warnings.");
        }
    }

    private void clearVariable(Variable variable) throws Exception {
        List<Register> registers = new ArrayList<>();
        toolbox.fillList(registers, new SimpleRegister((byte) 0, (byte) 0), variable.getCount());
        modbusClient.writeRegisters(variable.getAddress(), registers);
    }

    public void clickButton(Button button) {
        try {
            modbusClient.writeCoil(button.getAddress(), true);
        } catch (Exception e) {
            log.error("Failed to reset button coil. Button: {}", button, e);
        }
    }

    public void unclickButton(Button button) {
        try {
            modbusClient.writeCoil(button.getAddress(), false);
        } catch (Exception e) {
            log.error("Failed to reset button coil. Button: {}", button, e);
        }
    }

    public void handleManualEntryButtonClickEvent() {
        try {
            clearSessionVariables();

        } catch (Exception e) {
            log.error("Failed to handle abort button click event.", e);
        } finally {
            // todo publish message to destination.
        }
    }

    private void renderOptionButtonLabels() {

    }

    private void renderDetailPages() {

    }

    private void handleDoneButtonClickEvent() {
        decorateMessageWithVariables();
    }

    private void handleNextOptionsPageButtonClickEvent() {
        // TODO decorate xml with next pages.
        renderOptionButtonLabels();
        renderDetailPages();
    }

    private void handlePreviousOptionsPageButtonClickEvent() {
        // TODO decorate xml with previous pages.
        renderOptionButtonLabels();
        renderDetailPages();
    }

    private void handleSaveButtonClickEvent() {
        //Do nothing.  This logic is handled on touchscreen.  Only added here for completeness.
    }

    public void setCurrentDetailsPageIndex(Integer selectedDetailsPageIndex) {
        this.currentDetailsPageIndex = selectedDetailsPageIndex;
    }

    public Integer getCurrentDetailsPageIndex() {
        return this.currentDetailsPageIndex;
    }

    private void handleDetailsPage1ButtonEvent() {
        setCurrentDetailsPageIndex(1);
    }

    private void handleDetailsPage2ButtonEvent() {
        setCurrentDetailsPageIndex(2);
    }

    private void handleDetailsPage3ButtonEvent() {
        setCurrentDetailsPageIndex(3);
    }

    private void handleDetailsPage4ButtonEvent() {
        setCurrentDetailsPageIndex(4);
    }

    private void handleAbortButtonClickEvent() {
        try {
            clearSessionVariables();
        } catch (Exception e) {
            log.error("Failed to handle abort button click event.", e);
        } finally {
            // todo publish message to destination.
        }
    }

    @Scheduled(fixedDelay = 1000) // Runs every 1 second
    public void listenForButtonClickEvents() {
        for (Button button : touchscreen.getButtons()) {
            try {
                if (modbusClient.readCoil(button.getAddress())) {
                    unclickButton(button);
                    switch (button.getType()) {
                        case ABORT_BUTTON:
                            handleAbortButtonClickEvent();
                            break;
                        case MANUAL_ENTRY_BUTTON:
                            handleManualEntryButtonClickEvent();
                            break;
                        case DONE_BUTTON:
                            handleDoneButtonClickEvent();
                            break;
                        case NEXT_OPTIONS_PAGE_BUTTON:
                            handleNextOptionsPageButtonClickEvent();
                            break;
                        case PREVIOUS_OPTIONS_PAGE_BUTTON:
                            handlePreviousOptionsPageButtonClickEvent();
                            break;
                        case SAVE_BUTTON:
                            handleSaveButtonClickEvent();
                            break;
                        case DETAILS_PAGE_1_BUTTON:
                            handleDetailsPage1ButtonEvent();
                            break;
                        case DETAILS_PAGE_2_BUTTON:
                            handleDetailsPage2ButtonEvent();
                            break;
                        case DETAILS_PAGE_3_BUTTON:
                            handleDetailsPage3ButtonEvent();
                            break;
                        case DETAILS_PAGE_4_BUTTON:
                            handleDetailsPage4ButtonEvent();
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}