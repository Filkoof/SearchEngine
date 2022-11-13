package searchengine.mapper;

import org.mapstruct.Mapper;
import searchengine.dto.PageDto;
import searchengine.entity.PageEntity;

@Mapper
public interface PageMapper {

    PageDto pageEntityToDto(PageEntity pageEntity);

    PageEntity pageDtoToEntity(PageDto webPageDto);
}
