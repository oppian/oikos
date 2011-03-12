package com.oppian.oikos.model;

import java.math.BigDecimal;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Entry {
	
	@DatabaseField(generatedId = true)
	private Long id;
	
	@DatabaseField(canBeNull = false)
	private BigDecimal amount;
	
	@DatabaseField
	private String description;
	
	@DatabaseField(canBeNull = false)
	private Date entryDate;
	
	public Entry() {
		
	}
	
	public Entry(String amount, String description) {
		this.amount = new BigDecimal(amount);
		this.description = description;
		this.entryDate = new Date();
	}

	@Override
	public String toString() {
		return "Entry [amount=" + amount + ", description=" + description
				+ ", entryDate=" + entryDate + ", id=" + id + "]";
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}
}
