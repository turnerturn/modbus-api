package com.toolbox.modbus.modbusapi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class Toolbox {
    public String listToCsv(List<?> list) {
        return list.stream().map(i -> i.toString()).collect(Collectors.joining(","));
    }

    /**
     * The listChunker function takes a list of items and the desired chunk size as
     * its
     * parameters. It converts the list to array and uses Java's System.arraycopy()
     * method to create sub-arrays and adds them to an ArrayList.
     * The function returns this ArrayList, which contains all the chunks.
     * 
     * @param list      The list to be chunked
     * @param chunkSize The desired chunk size
     * @return An ArrayList containing all the chunked lists.
     */
    public List<List<?>> chunkList(List<?> list, int chunkSize) {
        List<List<?>> chunkedList = new ArrayList<>();

        int totalSize = list.size();

        for (int i = 0; i < totalSize; i += chunkSize) {
            int end = Math.min(i + chunkSize, totalSize);
            List<?> chunk = list.subList(i, end);
            chunkedList.add(new ArrayList<>(chunk));
        }

        return chunkedList;
    }

    /**
     * This example uses a generic method fillList which can work with a list of any
     * type (List<T>). The method takes the list to fill (list), the object to fill
     * it with (objectToFill), and the size up to which it should be filled (size).
     * It then iteratively adds objectToFill to list until it reaches the specified
     * size.
     * 
     * @param <T>
     * @param list
     * @param objectToFill
     * @param size
     */
    public <T> void fillList(List<T> list, T objectToFill, int size) {
        for (int i = 0; i < size; i++) {
            list.add(objectToFill);
        }
    }

}