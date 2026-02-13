package com.sorturlbackend.service.impl;

import com.sorturlbackend.dto.response.ClickEventResponse;
import com.sorturlbackend.dto.response.UrlMappingResponse;
import com.sorturlbackend.entity.ClickEvent;
import com.sorturlbackend.entity.UrlMapping;
import com.sorturlbackend.entity.User;
import com.sorturlbackend.exception.ResourceNotFoundException;
import com.sorturlbackend.mapper.UrlMappingMapper;
import com.sorturlbackend.repository.ClickEventRepository;
import com.sorturlbackend.repository.UrlMappingRepository;

import com.sorturlbackend.service.UrlMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlMappingServiceImpl implements UrlMappingService {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickEventRepository clickEventRepository;
    private final UrlMappingMapper urlMappingMapper;

    private static final String SHORT_URL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_URL_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public UrlMappingResponse createShortUrl(String originalUrl, User user) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                String shortUrl = generateShortUrl();
                UrlMapping urlMapping = UrlMapping.builder()
                        .originalUrl(originalUrl)
                        .shortUrl(shortUrl)
                        .user(user)
                        .createdDate(LocalDateTime.now())
                        .clickCount(0)
                        .build();

                UrlMapping savedMapping = urlMappingRepository.save(urlMapping);
                return urlMappingMapper.toResponse(savedMapping);
            } catch (Exception e) {
                // If collision or other error, retry
                attempts++;
                if (attempts == 3) {
                    throw new RuntimeException("Failed to generate unique short URL after 3 attempts", e);
                }
            }
        }
        throw new RuntimeException("Failed to generate unique short URL");
    }

    @Override
    public List<UrlMappingResponse> getUrlsByUser(User user) {
        List<UrlMapping> mappings = urlMappingRepository.findByUser(user);
        return urlMappingMapper.toResponseList(mappings);
    }

    @Override
    public List<ClickEventResponse> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        UrlMapping mapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (mapping == null) {
            return List.of();
        }

        Map<LocalDate, Long> clicksByDate = clickEventRepository
                .findByUrlMappingAndClickDateBetween(mapping, start, end)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getClickDate().toLocalDate(),
                        Collectors.counting()));

        return urlMappingMapper.toClickEventResponseList(clicksByDate);
    }

    @Override
    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        List<UrlMapping> mappings = urlMappingRepository.findByUser(user);

        return clickEventRepository
                .findByUrlMappingInAndClickDateBetween(
                        mappings,
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getClickDate().toLocalDate(),
                        Collectors.counting()));
    }

    @Override
    @Transactional
    public String getOriginalUrlAndRecordClick(String shortUrl) {
        UrlMapping mapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (mapping == null) {
            return null;
        }

        mapping.setClickCount(mapping.getClickCount() + 1);
        urlMappingRepository.save(mapping);

        ClickEvent event = ClickEvent.builder()
                .clickDate(LocalDateTime.now())
                .urlMapping(mapping)
                .build();
        clickEventRepository.save(event);

        return mapping.getOriginalUrl();
    }

    @Override
    @Transactional
    public void deleteUrl(String shortUrl, User user) {
        UrlMapping mapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (mapping == null) {
            throw new ResourceNotFoundException("URL", "shortUrl", shortUrl);
        }
        if (!mapping.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this URL");
        }
        urlMappingRepository.delete(mapping);
    }

    private String generateShortUrl() {
        StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            sb.append(SHORT_URL_CHARS.charAt(RANDOM.nextInt(SHORT_URL_CHARS.length())));
        }
        return sb.toString();
    }
}
