package com.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.community.entity.ReturnEntity;
import com.community.service.ReturnService;

@RestController
@RequestMapping("/returns")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @PostMapping("/{bookingId}")
    public ReturnEntity returnItem(@PathVariable Long bookingId) {
        return returnService.returnItem(bookingId);
    }
}
