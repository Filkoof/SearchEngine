package searchengine.mapper;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import searchengine.dto.PageDto;
import searchengine.entity.PageEntity;
import static org.junit.jupiter.api.Assertions.*;

class WebPageMapperTest {

    private static final int ID = 1;
    private static final String PATH = "https://github.com/Filkoof";
    private static final int CODE = 200;
    private static final String CONTENT = "<!DOCTYPE html><html><head>";
    private final PageMapper pageMapper = Mappers.getMapper(PageMapper.class);

    //TODO: решить вопрос с setSite

    @Test
    @DisplayName("Map Entity to DTO")
    void pageEntityToDto() {
        assertNotNull(pageMapper);

        PageEntity pageEntity = new PageEntity();
        pageEntity.setId(ID).setSite(ID).setPath(PATH).setCode(CODE).setContent(CONTENT);

        PageDto pageDto = new PageDto();
        pageDto.setId(ID).setSiteId(ID).setPath(PATH).setCode(CODE).setContent(CONTENT);

        assertEquals(pageDto, pageMapper.pageEntityToDto(pageEntity), "Не верный маппинг");
    }

    @Test
    @DisplayName("Map DTO to Entity")
    void pageDtoToEntity() {
        assertNotNull(pageMapper);

        PageDto pageDto = new PageDto();
        pageDto.setId(ID).setSiteId(ID).setPath(PATH).setCode(CODE).setContent(CONTENT);

        PageEntity pageEntity = pageMapper.pageDtoToEntity(pageDto);

        assertAll(() -> assertEquals(pageDto.getId(), pageEntity.getId()),
                () -> assertEquals(pageDto.getSiteId(), pageEntity.getSite()),
                () -> assertEquals(pageDto.getPath(), pageEntity.getPath()),
                () -> assertEquals(pageDto.getCode(), pageEntity.getCode()),
                () -> assertEquals(pageDto.getContent(), pageEntity.getContent())
        );
    }
}