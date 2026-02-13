package com.sorturlbackend.service;

import com.sorturlbackend.dto.response.ClickEventResponse;
import com.sorturlbackend.dto.response.UrlMappingResponse;
import com.sorturlbackend.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UrlMappingService {
    UrlMappingResponse createShortUrl(String originalUrl, User user);

    List<UrlMappingResponse> getUrlsByUser(User user);

    List<ClickEventResponse> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end);

    Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end);

    String getOriginalUrlAndRecordClick(String shortUrl);

    void deleteUrl(String shortUrl, User user);
}
