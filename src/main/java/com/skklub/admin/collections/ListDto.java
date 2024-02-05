package com.skklub.admin.collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListDto<T> {
    private Integer size = 0;
    private List<T> data = new ArrayList<>();

    public ListDto(List<T> data) {
        this.size = data.size();
        this.data = data;
    }
}
