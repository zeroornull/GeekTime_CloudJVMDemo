package de.tuotuo.cloudjvmdemo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DippedSauceCucumbers {

    private String mainIngredient = "黄瓜";
    private String dippingSauce = "豆瓣酱";
    private String menu = "";

    public DippedSauceCucumbers() {
        setMenu();
    }

    private void setMenu() {
        menu = "蘸酱小黄瓜{" +
            "mainIngredient='" + mainIngredient + '\'' +
            ", dippingSauce='" + dippingSauce + '\'' +
            '}';
    }

    //
    public void printMenu() {
        System.out.println(menu);
    }
    
    public static DippedSauceCucumbers createCustomizedFood() throws Exception {
        //
        Class dippedSauceCucumbersClass = DippedSauceCucumbers.class;
        
        //
        Object dippedSauceCucumbers = dippedSauceCucumbersClass.newInstance();
        
        //
        Field dippingSauceField  = dippedSauceCucumbersClass.getDeclaredField("dippingSauce");
        dippingSauceField.setAccessible(true);
        dippingSauceField.set(dippedSauceCucumbers, "沙拉酱");
        
        //
        Method setMenuMethod = dippedSauceCucumbersClass.getDeclaredMethod("setMenu");
        setMenuMethod.setAccessible(true);
        setMenuMethod.invoke(dippedSauceCucumbers);
        
        return (DippedSauceCucumbers)dippedSauceCucumbers;
    }

    public static void main(String[] args) throws Exception {
        DippedSauceCucumbers dippedSauceCucumbers = DippedSauceCucumbers.createCustomizedFood();
        dippedSauceCucumbers.printMenu();
    }

}
