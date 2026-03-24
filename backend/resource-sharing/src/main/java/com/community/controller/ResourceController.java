package com.community.controller;

import com.community.dto.ResourceRequest;
import com.community.dto.ResourceResponse;
import com.community.entity.Resource;
import com.community.entity.ResourceAvailability;
import com.community.entity.User;
import com.community.repository.ResourceRepository;
import com.community.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private static final String BASE_URL = "http://localhost:8080";

    public ResourceController(ResourceRepository resourceRepository,
                              UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // ------------------- ADD -------------------
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<ResourceResponse> addResource(@RequestBody ResourceRequest dto) {
        Resource resource = addResourceEntity(dto);
        return ResponseEntity.ok(mapToDTO(resource));
    }

    // ------------------- UPDATE -------------------
    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<ResourceResponse> updateResource(@PathVariable Long id,
                                                           @RequestBody ResourceRequest dto) {
        Resource resource = updateResourceEntity(id, dto);
        return ResponseEntity.ok(mapToDTO(resource));
    }

    // ------------------- DELETE -------------------
    @DeleteMapping("/delete/{id}")
    @Transactional
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        deleteResourceEntity(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------- PUBLIC -------------------
    @GetMapping("/public")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ResourceResponse>> getAllPublicResources() {
        List<Resource> resources = resourceRepository.findAllWithOwnerAndAvailability();
        List<ResourceResponse> dtoList = new ArrayList<>();
        for (Resource r : resources) dtoList.add(mapToDTO(r));
        return ResponseEntity.ok(dtoList);
    }

    // ------------------- OWNER -------------------
    @GetMapping("/owner")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ResourceResponse>> getOwnerResources() {
        User owner = getLoggedInUser();
        List<Resource> resources = resourceRepository.findByOwnerWithAvailability(owner);
        List<ResourceResponse> dtoList = new ArrayList<>();
        for (Resource r : resources) dtoList.add(mapToDTO(r));
        return ResponseEntity.ok(dtoList);
    }

    // ------------------- GET BY ID -------------------
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ResourceResponse> getResource(@PathVariable Long id) {
        Resource resource = getResourceEntityById(id);
        return ResponseEntity.ok(mapToDTO(resource));
    }

    // ------------------- SERVICE LOGIC -------------------
    private User getLoggedInUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails) ?
                ((UserDetails) principal).getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
    }

    private Resource addResourceEntity(ResourceRequest dto) {
        User owner = getLoggedInUser();
        Resource resource = mapDtoToResource(dto, owner);
        return resourceRepository.save(resource);
    }

    private Resource updateResourceEntity(Long resourceId, ResourceRequest dto) {
        User owner = getLoggedInUser();
        Resource existing = resourceRepository.findByIdWithOwnerAndAvailability(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!existing.getOwner().getUserId().equals(owner.getUserId()))
            throw new RuntimeException("You are not allowed to update this resource");

        existing.setTitle(dto.getTitle());
        existing.setCategory(dto.getCategory());
        existing.setDescription(dto.getDescription());
        existing.setRentPrice(dto.getRentPrice());
        existing.setDeposit(dto.getDeposit());
        existing.setCity(dto.getCity());
        existing.setArea(dto.getArea());
        existing.setLatitude(dto.getLatitude());
        existing.setLongitude(dto.getLongitude());
        existing.setImage(dto.getImage());
        existing.setMaxQuantity(dto.getMaxQuantity() != null && dto.getMaxQuantity() > 0 ? dto.getMaxQuantity() : existing.getMaxQuantity());

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<String> images = dto.getImages().size() > 3 ? dto.getImages().subList(0, 3) : dto.getImages();
            existing.setImages(images);
        } else existing.setImages(new ArrayList<>());

        existing.getAvailability().clear();
        if (dto.getAvailability() != null) {
            for (String date : dto.getAvailability()) {
                ResourceAvailability ra = new ResourceAvailability();
                ra.setAvailableDate(date);
                ra.setResource(existing);
                existing.getAvailability().add(ra);
            }
        }
        return resourceRepository.save(existing);
    }

    private void deleteResourceEntity(Long resourceId) {
        User owner = getLoggedInUser();
        Resource resource = resourceRepository.findByIdWithOwnerAndAvailability(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getOwner().getUserId().equals(owner.getUserId()))
            throw new RuntimeException("You are not allowed to delete this resource");

        resourceRepository.delete(resource);
    }

    private Resource getResourceEntityById(Long resourceId) {
        Resource resource = resourceRepository.findByIdWithOwnerAndAvailability(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        if (resource.getImages() != null) resource.getImages().size(); // force load
        return resource;
    }

    private Resource mapDtoToResource(ResourceRequest dto, User owner) {
        Resource resource = new Resource();
        resource.setTitle(dto.getTitle());
        resource.setCategory(dto.getCategory());
        resource.setDescription(dto.getDescription());
        resource.setRentPrice(dto.getRentPrice());
        resource.setDeposit(dto.getDeposit());
        resource.setCity(dto.getCity());
        resource.setArea(dto.getArea());
        resource.setLatitude(dto.getLatitude());
        resource.setLongitude(dto.getLongitude());
        resource.setImage(dto.getImage());
        resource.setMaxQuantity(dto.getMaxQuantity() != null && dto.getMaxQuantity() > 0 ? dto.getMaxQuantity() : 1);
        resource.setOwner(owner);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            resource.setImages(dto.getImages().size() > 3 ? dto.getImages().subList(0, 3) : dto.getImages());
        }

        List<ResourceAvailability> availability = new ArrayList<>();
        if (dto.getAvailability() != null) {
            for (String date : dto.getAvailability()) {
                ResourceAvailability ra = new ResourceAvailability();
                ra.setAvailableDate(date);
                ra.setResource(resource);
                availability.add(ra);
            }
        }
        resource.setAvailability(availability);
        return resource;
    }

    // ------------------- DTO MAPPING -------------------
    private ResourceResponse mapToDTO(Resource r) {
        ResourceResponse dto = new ResourceResponse();
        dto.setResourceId(r.getResourceId());
        dto.setTitle(r.getTitle() != null ? r.getTitle() : "");
        dto.setCategory(r.getCategory() != null ? r.getCategory() : "");
        dto.setDescription(r.getDescription() != null ? r.getDescription() : "");
        dto.setDeposit(r.getDeposit() != null ? r.getDeposit() : 0);
        dto.setRentPrice(r.getRentPrice() != null ? r.getRentPrice() : 0);
        dto.setCity(r.getCity() != null ? r.getCity() : "");
        dto.setArea(r.getArea() != null ? r.getArea() : "");
        dto.setLatitude(r.getLatitude() != null ? r.getLatitude() : 0);
        dto.setLongitude(r.getLongitude() != null ? r.getLongitude() : 0);
        dto.setMaxQuantity(r.getMaxQuantity() != null ? r.getMaxQuantity() : 1);

        String mainImage = (r.getImage() != null && !r.getImage().isEmpty()) ? r.getImage() : "/placeholder.jpg";
        dto.setImage(mainImage.startsWith("http") ? mainImage : BASE_URL + mainImage);

        dto.setImages(r.getImages() != null && !r.getImages().isEmpty()
                ? r.getImages().stream()
                .map(img -> img != null && !img.isEmpty() ? (img.startsWith("http") ? img : BASE_URL + img) : BASE_URL + "/placeholder.jpg")
                .toList()
                : List.of(dto.getImage()));

        dto.setAvailability(r.getAvailability() != null && !r.getAvailability().isEmpty()
                ? r.getAvailability().stream().map(ra -> ra.getAvailableDate() != null ? ra.getAvailableDate() : "").toList()
                : List.of());

        dto.setOwnerEmail(r.getOwner() != null ? r.getOwner().getEmail() : "");

        return dto;
    }
}
