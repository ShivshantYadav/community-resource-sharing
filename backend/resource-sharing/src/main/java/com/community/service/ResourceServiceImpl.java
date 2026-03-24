package com.community.service;

import com.community.dto.ResourceRequest;
import com.community.dto.ResourceResponse;
import com.community.entity.Resource;
import com.community.entity.ResourceAvailability;
import com.community.entity.User;
import com.community.repository.ResourceRepository;
import com.community.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private static final String BASE_URL = "http://localhost:8080/";

    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // -------------------- OWNER HELPERS --------------------
    private User getLoggedInUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails) ?
                ((UserDetails) principal).getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
    }

    // -------------------- ADD RESOURCE --------------------
    @Override
    public Resource addResource(ResourceRequest dto) {
        User owner = getLoggedInUser();
        Resource resource = mapDtoToResource(dto, owner);
        return resourceRepository.save(resource);
    }

    // -------------------- UPDATE RESOURCE --------------------
    @Override
    public Resource updateResource(Long resourceId, ResourceRequest dto) {
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
        existing.setMaxQuantity(
                dto.getMaxQuantity() != null && dto.getMaxQuantity() > 0
                        ? dto.getMaxQuantity()
                        : existing.getMaxQuantity()
        );




        // IMAGES (up to 3)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<String> images = dto.getImages().size() > 3 ? dto.getImages().subList(0, 3) : dto.getImages();
            existing.setImages(images);
        } else {
            existing.setImages(new ArrayList<>());
        }

        // AVAILABILITY
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

    // -------------------- DELETE RESOURCE --------------------
    @Override
    public void deleteResource(Long resourceId) {
        User owner = getLoggedInUser();
        Resource resource = resourceRepository.findByIdWithOwnerAndAvailability(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getOwner().getUserId().equals(owner.getUserId()))
            throw new RuntimeException("You are not allowed to delete this resource");

        resourceRepository.delete(resource);
    }
 
 // -------------------- OWNER RESOURCES (DTO) --------------------
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourcesByOwner() {
        User owner = getLoggedInUser();
        List<Resource> resources = resourceRepository.findByOwnerWithAvailability(owner);

        return resources.stream()
                .map(resource -> {
                    ResourceResponse response = new ResourceResponse();

                    response.setResourceId(resource.getResourceId());
                    response.setTitle(resource.getTitle() != null ? resource.getTitle() : "");
                    response.setCategory(resource.getCategory() != null ? resource.getCategory() : "");
                    response.setDescription(resource.getDescription() != null ? resource.getDescription() : "");
                    response.setDeposit(resource.getDeposit() != null ? resource.getDeposit() : 0);
                    response.setRentPrice(resource.getRentPrice() != null ? resource.getRentPrice() : 0);
                    response.setCity(resource.getCity() != null ? resource.getCity() : "");
                    response.setArea(resource.getArea() != null ? resource.getArea() : "");
                    response.setLatitude(resource.getLatitude() != null ? resource.getLatitude() : 0);
                    response.setLongitude(resource.getLongitude() != null ? resource.getLongitude() : 0);

                    // ✅ MAX QUANTITY
                    response.setMaxQuantity(resource.getMaxQuantity() != null ? resource.getMaxQuantity() : 1);

                    // MAIN IMAGE
                    String mainImage = (resource.getImage() != null && !resource.getImage().isEmpty())
                            ? resource.getImage()
                            : "/placeholder.jpg";
                    response.setImage(mainImage.startsWith("http") ? mainImage : BASE_URL + mainImage);

                    // ADDITIONAL IMAGES
                    if (resource.getImages() != null && !resource.getImages().isEmpty()) {
                        response.setImages(
                                resource.getImages().stream()
                                        .map(img -> img != null && !img.isEmpty()
                                                ? (img.startsWith("http") ? img : BASE_URL + img)
                                                : BASE_URL + "/placeholder.jpg")
                                        .toList()
                        );
                    } else {
                        response.setImages(List.of(response.getImage()));
                    }

                    // AVAILABILITY
                    if (resource.getAvailability() != null && !resource.getAvailability().isEmpty()) {
                        response.setAvailability(
                                resource.getAvailability().stream()
                                        .map(ra -> ra.getAvailableDate() != null ? ra.getAvailableDate() : "")
                                        .toList()
                        );
                    } else {
                        response.setAvailability(List.of());
                    }

                    // OWNER EMAIL
                    response.setOwnerEmail(resource.getOwner() != null && resource.getOwner().getEmail() != null
                            ? resource.getOwner().getEmail()
                            : "");

                    return response;
                })
                .toList();
    }



    // -------------------- GET RESOURCE BY ID --------------------
    @Override
    public Resource getResourceById(Long resourceId) {
        Resource resource = resourceRepository.findByIdWithOwnerAndAvailability(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        if (resource.getImages() != null) resource.getImages().size(); // force load images
        return resource;
    }

    // -------------------- PUBLIC RESOURCES (SAFE FOR FRONTEND) --------------------
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        List<Resource> resources = resourceRepository.findAllWithOwnerAndAvailability();

        return resources.stream()
                .map(resource -> {
                    ResourceResponse response = new ResourceResponse();

                    response.setResourceId(resource.getResourceId());
                    response.setTitle(resource.getTitle() != null ? resource.getTitle() : "");
                    response.setCategory(resource.getCategory() != null ? resource.getCategory() : "");
                    response.setDescription(resource.getDescription() != null ? resource.getDescription() : "");
                    response.setDeposit(resource.getDeposit() != null ? resource.getDeposit() : 0);
                    response.setRentPrice(resource.getRentPrice() != null ? resource.getRentPrice() : 0);
                    response.setCity(resource.getCity() != null ? resource.getCity() : "");
                    response.setArea(resource.getArea() != null ? resource.getArea() : "");
                    response.setLatitude(resource.getLatitude() != null ? resource.getLatitude() : 0);
                    response.setLongitude(resource.getLongitude() != null ? resource.getLongitude() : 0);
                    response.setMaxQuantity(
                            resource.getMaxQuantity() != null ? resource.getMaxQuantity() : 1
                    );


                    // MAIN IMAGE
                    String mainImage = (resource.getImage() != null && !resource.getImage().isEmpty())
                            ? resource.getImage()
                            : "/placeholder.jpg";
                    response.setImage(mainImage.startsWith("http") ? mainImage : BASE_URL + mainImage);

                    // ADDITIONAL IMAGES
                    if (resource.getImages() != null && !resource.getImages().isEmpty()) {
                        response.setImages(
                                resource.getImages().stream()
                                        .map(img -> img != null && !img.isEmpty() ? (img.startsWith("http") ? img : BASE_URL + img) : BASE_URL + "/placeholder.jpg")
                                        .toList()
                        );
                    } else {
                        response.setImages(List.of(response.getImage()));
                    }

                    // AVAILABILITY
                    if (resource.getAvailability() != null && !resource.getAvailability().isEmpty()) {
                        response.setAvailability(
                                resource.getAvailability().stream()
                                        .map(ra -> ra.getAvailableDate() != null ? ra.getAvailableDate() : "")
                                        .toList()
                        );
                    } else {
                        response.setAvailability(List.of());
                    }

                    // OWNER EMAIL
                    if (resource.getOwner() != null && resource.getOwner().getEmail() != null) {
                        response.setOwnerEmail(resource.getOwner().getEmail());
                    } else {
                        response.setOwnerEmail("");
                    }

                    return response;
                })
                .toList();
    }


    // -------------------- DTO MAPPER --------------------
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
        resource.setMaxQuantity(
                dto.getMaxQuantity() != null && dto.getMaxQuantity() > 0
                        ? dto.getMaxQuantity()
                        : 1
        );


        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<String> images = dto.getImages().size() > 3 ? dto.getImages().subList(0, 3) : dto.getImages();
            resource.setImages(images);
        }

        resource.setOwner(owner);

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
}
