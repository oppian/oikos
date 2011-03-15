package com.oppian.oikos.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Account implements Serializable {
    
    private static final long serialVersionUID = 2044539981769509541L;

    @DatabaseField(generatedId=true)
    private Integer id;
    
    @DatabaseField(canBeNull = false)
    private String name;
    
    @DatabaseField(canBeNull = false)
    private int total;
    
    public Account() {
        
    }
    
    public Account(String name, int total) {
        this.name = name;
        this.total = total;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Account [id=" + id + ", name=" + name + ", total=" + total + "]";
    }

}
