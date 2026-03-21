package com.mipt.nikitabumagin.dto.mapper;

import com.mipt.nikitabumagin.dto.TaskAttachmentCreateDto;
import com.mipt.nikitabumagin.dto.TaskAttachmentResponseDto;
import com.mipt.nikitabumagin.dto.TaskAttachmentUpdateDto;
import com.mipt.nikitabumagin.model.TaskAttachment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskAttachmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storedFileName", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    TaskAttachment toEntity(TaskAttachmentCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    TaskAttachment updateEntity(TaskAttachmentUpdateDto dto,
            @MappingTarget TaskAttachment attachment);

    TaskAttachmentResponseDto toResponseDto(TaskAttachment attachment);
}
