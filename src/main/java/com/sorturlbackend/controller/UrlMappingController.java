package com.sorturlbackend.controller;

import com.sorturlbackend.dto.request.CreateUrlRequest;
import com.sorturlbackend.dto.response.ClickEventResponse;
import com.sorturlbackend.dto.response.UrlMappingResponse;
import com.sorturlbackend.entity.User;
import com.sorturlbackend.service.UrlMappingService;
import com.sorturlbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/urls")
@RequiredArgsConstructor
public class UrlMappingController {

    private final UrlMappingService urlMappingService;
    private final UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request,
            Principal principal) {
        User user = userService.findByUsername(principal.getName());
        UrlMappingResponse response = urlMappingService.createShortUrl(request.getOriginalUrl(), user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-urls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingResponse>> getUserUrls(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<UrlMappingResponse> urls = urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventResponse>> getUrlAnalytics(
            @PathVariable String shortUrl,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);

        List<ClickEventResponse> clickEvents = urlMappingService.getClickEventsByDate(shortUrl, start, end);
        return ResponseEntity.ok(clickEvents);
    }

    @GetMapping("/total-clicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicksByDate(
            Principal principal,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        User user = userService.findByUsername(principal.getName());
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        Map<LocalDate, Long> totalClicks = urlMappingService.getTotalClicksByUserAndDate(user, start, end);
        return ResponseEntity.ok(totalClicks);
    }

    @DeleteMapping("/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortUrl, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        urlMappingService.deleteUrl(shortUrl, user);
        return ResponseEntity.noContent().build();
    }
}
