package org.jboss.seam.example.pdf;

import org.jboss.seam.annotations.Name;


@Name("lists")
public class Lists{

    private String[] numbers = {"one", "two", "three", "four", "five"};
    private String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    
    
    public String[] getNumbers() {
        return numbers;
    }

    public String[] getDaysOfWeek() {
        return daysOfWeek;
    }
}
