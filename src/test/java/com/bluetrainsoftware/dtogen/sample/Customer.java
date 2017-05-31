package com.bluetrainsoftware.dtogen.sample;

import javax.persistence.Entity;
import java.util.Set;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@Entity
public class Customer {
	private Long id;
	private String name;
	private Set<Order> orders;
	private Order[] minOrders;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Order> getOrders() {
		return orders;
	}

	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}

	public Order[] getMinOrders() {
		return minOrders;
	}

	public void setMinOrders(Order[] minOrders) {
		this.minOrders = minOrders;
	}
}
