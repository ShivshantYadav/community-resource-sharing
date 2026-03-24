package com.community.service;

import com.community.dto.ResourceRequest;
import com.community.entity.Resource;

import java.util.List;

public interface ResourceService {
    Resource addResource(ResourceRequest dto);
    Resource updateResource(Long resourceId, ResourceRequest dto);
    void deleteResource(Long resourceId);

    List<Resource> getResourcesByOwner();
    Resource getResourceById(Long resourceId);
    List<Resource> getAllResources();
}
