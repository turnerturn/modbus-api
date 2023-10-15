package com.toolbox.modbus.tcplistener;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component
public class ModbusApiClient {
  private  RestTemplate restTemplate = new RestTemplate();
    public void readFromModbusRegisters(Integer offset, Integer count, ModbusDataType dataType) {
        // Define the URL for the POST request
        String url = "http://localhost:8080/api/modbus/registers/read";

        // Create headers with the desired content type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request body (replace with your JSON payload)
        String requestBody = "{\"offset\": "+offset+",\"count\": "+count+",\"dataType\": \""+dataType.getValue()+"\"}";
        // Create an HttpEntity with headers and request body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send the POST request and get the response
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

        // Process the response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            System.out.println("POST Request Successful. Response: " + responseBody);
        } else {
            System.err.println("POST Request Failed. Status code: " + responseEntity.getStatusCodeValue());
        }
    }
    public void writeToModbusRegisters(Integer offset, String data, ModbusDataType dataType) {
        // Define the URL for the POST request
        String url = "http://localhost:8080/api/modbus/registers/write";

        // Create headers with the desired content type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request body (replace with your JSON payload)
        String requestBody = "{\"offset\": "+offset+",\"data\": \""+data+"\",\"dataType\": \""+dataType.getValue()+"\"}";
        // Create an HttpEntity with headers and request body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send the POST request and get the response
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

        // Process the response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            System.out.println("POST Request Successful. Response: " + responseBody);
        } else {
            System.err.println("POST Request Failed. Status code: " + responseEntity.getStatusCodeValue());
        }
    }
      public void clearModbusRegisters(Integer offset, Integer count) {
         // Define the URL for the POST request
        String url = "http://localhost:8080/api/modbus/registers/clear";

        // Create headers with the desired content type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request body (replace with your JSON payload)
        String requestBody = "{\"offset\": "+offset+",\"count\": "+count + "}";
        // Create an HttpEntity with headers and request body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send the POST request and get the response
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

        // Process the response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            System.out.println("POST Request Successful. Response: " + responseBody);
        } else {
            System.err.println("POST Request Failed. Status code: " + responseEntity.getStatusCodeValue());
        }
      }
}
enum ModbusDataType{
    STRING("string"),
    DINT("dint"),
    LOW_BYTE("low-byte"),
    HIGH_BYTE("high-byte");
    private String value;
    ModbusDataType(String value){
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }   

}
