package com.community.entity;

import com.community.dto.ResourceResponse;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    @Column(name = "name", nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Double rentPrice;

    private Double deposit;

    private String city;
    private String area;
    private Double latitude;
    private Double longitude;
    
    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity = 1;

    private String image; // Main image (can be URL or relative path)

    @ElementCollection
    @CollectionTable(
        name = "resource_images",
        joinColumns = @JoinColumn(name = "resource_id")
    )
    @Column(name = "image_url", nullable = false)
    private List<String> images = new ArrayList<>(); // All images

    @OneToMany(
        mappedBy = "resource",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference
    private List<ResourceAvailability> availability = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    public ResourceResponse toResponse() {
        ResourceResponse dto = new ResourceResponse();
        dto.setResourceId(this.resourceId);
        dto.setTitle(this.title);
        dto.setCategory(this.category);
        dto.setDescription(this.description);
        dto.setDeposit(this.deposit);
        dto.setRentPrice(this.rentPrice);
        dto.setCity(this.city);
        dto.setArea(this.area);
        dto.setLatitude(this.latitude);
        dto.setLongitude(this.longitude);
        dto.setMaxQuantity(this.maxQuantity);
        dto.setImage(this.image);
        dto.setImages(this.images);
        
        // Optional: populate availability if you want
        if (this.getAvailability() != null) {
            List<String> availList = new ArrayList<>();
            this.getAvailability().forEach(a -> availList.add(a.toString())); // or customize
            dto.setAvailability(availList);
        }
        
        // Simplified owner info
        if (this.getOwner() != null) {
            dto.setOwnerEmail(this.getOwner().getEmail());
        }
        
        return dto;
    }


    // ----------- Getters / Setters -----------

    public Long getResourceId() { return resourceId; }
    
    public Integer getMaxQuantity() {
		return maxQuantity;
	}
	public void setMaxQuantity(Integer maxQuantity) {
		this.maxQuantity = maxQuantity;
	}
	public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getRentPrice() { return rentPrice; }
    public void setRentPrice(Double rentPrice) { this.rentPrice = rentPrice; }

    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public List<ResourceAvailability> getAvailability() { return availability; }
    public void setAvailability(List<ResourceAvailability> availability) { this.availability = availability; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    // ----------- Helper for frontend -----------

    /**
     * Returns a list of full URLs for images.
     * If the image path is relative, prepend backend URL.
     */
    @JsonProperty("imageUrls")
    public List<String> getImageUrls() {
        String baseUrl = "http://localhost:8080"; // change if backend URL is different
        List<String> urls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            for (String img : images) {
                if (img == null || img.isEmpty()) {
                    urls.add("/placeholder.jpg");
                } else if (img.startsWith("http")) {
                    urls.add(img);
                } else {
                    urls.add(baseUrl + "/" + img);
                }
            }
        } else if (image != null && !image.isEmpty()) {
            urls.add(image.startsWith("http") ? image : baseUrl + "/" + image);
        } else {
            urls.add("/placeholder.jpg");
        }

        return urls;
    }


	 
}
