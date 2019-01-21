package com.mas;

import java.sql.Array;

public class Main
{
    public static void main(String[] args)
    {
        Parser parser = new Parser();
        String[] array = {"01","02","03","04","05","06","07","08","09","10","11"};
        for (String number : array){
            parser.readTextFile(number);
            parser.createXML(number);
        }
    }
}
