package com.hoangdinh.delta_shop_app.dto.response.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionResponse {
    private String keyword;
    private List<AutoCompleteResponse> suggestions;
    private List<String> popularKeywords;
}
