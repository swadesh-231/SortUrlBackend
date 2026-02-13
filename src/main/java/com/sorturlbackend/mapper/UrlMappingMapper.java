package com.sorturlbackend.mapper;


import com.sorturlbackend.dto.response.ClickEventResponse;
import com.sorturlbackend.dto.response.UrlMappingResponse;
import com.sorturlbackend.entity.UrlMapping;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UrlMappingMapper {

    public UrlMappingResponse toResponse(UrlMapping entity) {
        if (entity == null) {
            return null;
        }
        return UrlMappingResponse.builder()
                .id(entity.getId())
                .originalUrl(entity.getOriginalUrl())
                .shortUrl(entity.getShortUrl())
                .clickCount(entity.getClickCount())
                .createdDate(entity.getCreatedDate())
                .username(entity.getUser() != null ? entity.getUser().getName() : null)
                .build();
    }

    public List<UrlMappingResponse> toResponseList(List<UrlMapping> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ClickEventResponse> toClickEventResponseList(Map<LocalDate, Long> clicksByDate) {
        return clicksByDate.entrySet().stream()
                .map(entry -> ClickEventResponse.builder()
                        .clickDate(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
