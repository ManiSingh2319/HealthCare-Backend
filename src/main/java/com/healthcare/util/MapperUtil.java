package com.healthcare.util;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapperUtil {
    private final ModelMapper modelMapper;

    public <T> T map(Object source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }
}
