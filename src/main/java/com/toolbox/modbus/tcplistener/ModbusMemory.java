package com.toolbox.modbus.tcplistener;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class ModbusMemory {
    /*
     Pipelines Table:

PipelineID: Register 40001, INT16
Name: Register 40002-40021, ASCII String (20 characters)
Location: Register 40022-40041, ASCII String (20 characters)
Capacity: Register 40042-40043, FLOAT32
Operator: Register 40044-40063, ASCII String (20 characters)
StartDate: Register 40064-40067, UNIX timestamp (32-bit integer)
Materials Table:

MaterialID: Register 40068, INT16
PipelineID: Register 40069, INT16
MaterialType: Register 40070-40089, ASCII String (20 characters)
Quantity: Register 40090, INT16
Operators Table:

OperatorID: Register 40091, INT16
Name: Register 40092-40111, ASCII String (20 characters)
ContactInfo: Register 40112-40131, ASCII String (20 characters)
     */
    public IngredientsDirectory getIngredientsDirectory(){
        return new IngredientsDirectory(0, 0, 0, null);
    }
     public List<Ingredient> findIngredients(){
        IngredientsDirectory dir = getIngredientsDirectory();
        for(int i = dir.getAddress(); i < dir.getAddress()+dir.getCount(); i+=dir.getRegistersPerIngedient()){
            //init ingreditent from each element.
        }
        return null;
     }
      public void removeIngredientsByCode(String code){
         IngredientsDirectory dir = getIngredientsDirectory();
       List<Ingredient> ingredients = findIngredientsByCode(code);
       dir.setIngredients(ingredients);

      }

     public List<Ingredient> findIngredientsByCode(String code){
        return findIngredients().stream().filter(i -> i.getCode().equals(code)).toList();
     }
      private void saveIngredientsDirectory(IngredientsDirectory dir){
        //write dir to modbus directory
      }
     //do this for other directories
}
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class IngredientsDirectory{
    //modbus address of the items directory
    private int address;
    //The count of registers reserved for our items directory
    private int count;
    //The number of registers reserved for each item
    private int registersPerIngedient;
    private List<Ingredient> ingredients;

}
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
 class Ingredient{
    private String code;
    private String name;
    private String description;
 
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class RecipesDirectory {
        //modbus address of the items directory
    private int address;
    //The count of registers reserved for our items directory
    private int count;
    //The number of registers reserved for each item
    private int registersPerRecipe;
    private List<Recipe> recipes;
}
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class Recipe {
    private String code;
    private String name;
    private Integer ingredient1Index;  
    private Integer ingredient1VendorIndex;
    private Integer ingredient1Quantity;
    private Integer ingredient2Index;  
    private Integer ingredient2VendorIndex;
    private Integer ingredient2Quantity;
     private Integer ingredient3Index;  
    private Integer ingredient3VendorIndex;
    private Integer ingredient3Quantity;

    
    //Up to 20 ingredients?
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class OrdersDirectory {
        //modbus address of the items directory
    private int address;
    //The count of registers reserved for our items directory
    private int count;
    //The number of registers reserved for each item
    private int registersPerLoad;
    private List<Recipe> recipes;
}
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class Order {
    private String poNumber;
    private Integer recipeIndex;
    private Integer quantity;
}