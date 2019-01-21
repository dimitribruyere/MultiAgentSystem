package com.mas;

import java.util.ArrayList;
import java.util.List;

public class Domain
{
    private int id;

    private int size;

    private List<Integer> values;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public List<Integer> getValues()
    {
        return values;
    }

    public void setValues(List<Integer> values)
    {
        this.values = values;
    }
}
