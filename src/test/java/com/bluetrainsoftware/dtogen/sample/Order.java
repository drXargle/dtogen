package com.bluetrainsoftware.dtogen.sample;

import javax.persistence.Entity;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@Entity
public class Order {
	private Long id;
	private Customer customer;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
