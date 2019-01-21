package com.mas;

public class Main
{
    public static void main(String[] args)
    {
        Parser parser = new Parser();
        parser.readTextFile("01");
        parser.createXML();
    }
}
