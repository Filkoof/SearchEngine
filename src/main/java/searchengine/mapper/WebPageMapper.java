package searchengine.mapper;

import org.mapstruct.Mapper;
import searchengine.dto.PageDto;
import searchengine.entity.PageEntity;

@Mapper(componentModel = "spring")
public interface WebPageMapper {

    PageDto webPageEntityToDto(PageEntity webPageEntity);

    PageEntity webPageDtoToEntity(PageDto webPageDto);
}
