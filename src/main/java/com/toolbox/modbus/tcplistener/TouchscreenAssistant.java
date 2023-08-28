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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
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

    private String xmlMessage;
    @PostConstruct
    private void init() {
        currentOptionsPage = getFirstOptionsPageIndex();
        currentDetailsPageIndex = 1;
    }

    public void process(String xmlMessage) throws Exception {
        this.xmlMessage = xmlMessage;
        clearSessionVariables();
        clearPageAttributes(xmlMessage);
        // TODO Save xml object to memory
        // assign page number attribute to xml elements
        assignPageNumberAttributes(xmlMessage);

        // extract variables from elements where page > 0
        decorateVariablesWithMessage( xmlMessage);
        writeAllVariables();
    }
    
    public Integer getCurrentOptionsPage() {
        return currentOptionsPage;
    }
    public void setCurrentOptionsPage(Integer currentOptionsPage) {
        this.currentOptionsPage = currentOptionsPage;
    }
    public Integer getFirstOptionsPageIndex() {
        return 1;
    }

    public Integer getLastOptionsPageIndex() {
        return touchscreen.getPageCount();
    }

    public Integer getNextOptionsPageIndex() {
        int currentPageIndex = getCurrentOptionsPage();
        if (currentPageIndex + 1 > getLastOptionsPageIndex()) {
            return getFirstOptionsPageIndex();
        } else {
            return currentPageIndex + 1;
        }
    }

    public Integer getPreviousOptionsPageIndex() {
        int currentPageIndex = getCurrentOptionsPage();
        if (currentPageIndex - 1 < getFirstOptionsPageIndex()) {
            return getLastOptionsPageIndex();
        } else {
            return currentPageIndex - 1;
        }
    }


    private void decorateVariablesWithMessage(String xmlMessage) {
        touchscreen.getVariables().stream().forEach(v -> {
            try {
                String value = extractTextValue(xmlMessage, v.getExpression());
                v.setValue(value);
            } catch (Exception e) {
                  log.warn("Failed to decorate variables with text values extracted from xml essage.", e);
            }
        });
    }

    private void decorateMessageWithVariables(String xmlMessage) {
        touchscreen.getVariables().stream().forEach(v -> {
            try {
               setTextValueWithExpressionAndValue(xmlMessage, v.getExpression(), v.getValue());
            } catch (Exception e) {
                log.warn("Failed to decorate xml message from variable values.", e);
            }
        });
    }

    protected void syncVariableValuesWithModbus(){
        //TODO reads each variables value from modbus and sets variable.value
    }
    protected void writeAllVariables() {
        touchscreen.getVariables().stream().forEach(this::writeVariable);
    }

    protected List<Variable> readAllVariables() {
        List<Variable> variables = new ArrayList<>();
        touchscreen.getVariables().stream().map(this::readVariable).forEach(variables::add);
        ;
        return variables;
    }

    protected void writeVariable(Variable variable) {
        try {
            modbusClient.writeStringValueToRegisters(variable.getAddress(), variable.getCount(), variable.getValue());
        } catch (Exception e) {
            log.warn("Failed to write variable.  Variable: {}", variable, e);
        }
    }

    protected Variable readVariable(Variable variable) {
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

    protected void clearVariable(Variable variable) throws Exception {
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

    protected void handleManualEntryButtonClickEvent() {
        try {
            clearSessionVariables();

        } catch (Exception e) {
            log.error("Failed to handle abort button click event.", e);
        } finally {
            // todo publish message to destination.
        }
    }

    protected void renderOptionButtonLabels() {

    }

    protected void renderDetailPages() {

    }

    protected void handleDoneButtonClickEvent() {
        syncVariableValuesWithModbus();
        decorateMessageWithVariables(xmlMessage);
        //TODO respond to sender
        //TODO display message on touchscreen
    }
    
    private void handleNextOptionsPageButtonClickEvent() {
        setCurrentOptionsPage(getNextOptionsPageIndex());
        renderOptionButtonLabels();
        renderDetailPages();
    }

    private void handlePreviousOptionsPageButtonClickEvent() {
        setCurrentOptionsPage(getPreviousOptionsPageIndex());
        renderOptionButtonLabels();
        renderDetailPages();
    }

    private void handleSaveButtonClickEvent() {
        // Do nothing. This logic is handled on touchscreen. Only added here for
        // completeness.
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

    private String clearPageAttributes(String xml) throws Exception {
        // Selects the first 4 book elements that are children of the bookstore element
        String xpathExpression = "/bookstore/book";
        List<Element> elements = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element) nodeList.item(i);
            elem.setAttribute("page", ""); // Setting attribute here
            elements.add(elem);
        }

        // If you want to save the modified XML back to a string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new java.io.StringWriter());
        transformer.transform(source, result);
        String modifiedXmlString = result.getWriter().toString();

        log.info("Modified XML: \n {}", modifiedXmlString);

        return modifiedXmlString;
    }


    /**
     * Extracts a text value from an XML string using an XPath expression.
     *
     * @param xml             the xml string
     * @param xpathExpression the XPath expression to evaluate
     * @return the text value as a String
     * @throws Exception if an XPath expression evaluation error occurs
     */
    private  String extractTextValue(String xml, String xpathExpression) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        Node node = (Node) xpath.evaluate(xpathExpression, doc, XPathConstants.NODE);
        if (node == null) {
            throw new Exception("Node not found for the XPath expression: " + xpathExpression);
        }

        return node.getTextContent();
    }
/**
 * Sets the text value of an XML element identified by an XPath expression.
 *
 * @param xml             the XML string 
 * @param xpathExpression the XPath expression to evaluate
 * @param newValue        the new text value to set
 * @throws Exception if an XPath expression evaluation error occurs
 */
public static void setTextValueWithExpressionAndValue(String xml, String xpathExpression, String newValue) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();

    Node node = (Node) xpath.evaluate(xpathExpression, doc, XPathConstants.NODE);
    if (node == null) {
        throw new Exception("Node not found for the XPath expression: " + xpathExpression);
    }

    node.setTextContent(newValue);
}
    private  List<Element> assignPageNumberAttributes(String xml) throws Exception {
        int itemIndexStart = ((getCurrentOptionsPage() - 1)  * touchscreen.getPageSize());
        int itemIndexEnd = itemIndexStart +  touchscreen.getPageSize();
       // Selects the first 4 book elements that are children of the bookstore element
        String xpathExpression = "/bookstore/book[position()> "+itemIndexStart+" and position()<= "+itemIndexEnd+"]";
        List<Element> elements = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element) nodeList.item(i);
            elem.setAttribute("page", String.valueOf(getCurrentOptionsPage()));  // Setting attribute here
            elements.add(elem);
        }

        // If you want to save the modified XML back to a string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new java.io.StringWriter());
        transformer.transform(source, result);
        String modifiedXmlString = result.getWriter().toString();

        System.out.println("Modified XML: \n" + modifiedXmlString);

        return elements;
    }

}
