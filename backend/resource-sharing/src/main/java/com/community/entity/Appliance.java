package com.community.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("APPLIANCE")
public class Appliance extends Resource {
    private String brand;

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Appliance(String brand) {
		super();
		this.brand = brand;
	}

	public Appliance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Appliance(Long resourceId, String name, String category, String description, Double rentPrice,
			Double depositAmount, ResourceStatus status, User owner) {
		super(resourceId, name, category, description, rentPrice, depositAmount, status, owner);
		// TODO Auto-generated constructor stub
	}
}

