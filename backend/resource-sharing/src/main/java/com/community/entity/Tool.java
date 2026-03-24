package com.community.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TOOL")
public class Tool extends Resource {
    private String toolType;

	public Tool() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Tool(Long resourceId, String name, String category, String description, Double rentPrice,
			Double depositAmount, ResourceStatus status, User owner) {
		super(resourceId, name, category, description, rentPrice, depositAmount, status, owner);
	}

	public Tool(String toolType) {
		super();
		this.toolType = toolType;
	}

	public String getToolType() {
		return toolType;
	}

	public void setToolType(String toolType) {
		this.toolType = toolType;
	}
}
