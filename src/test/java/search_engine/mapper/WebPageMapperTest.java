package search_engine.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import search_engine.ContextLoad;
import search_engine.dto.PageDto;
import search_engine.entity.PageEntity;
import search_engine.entity.SiteEntity;
import search_engine.entity.enumerated.StatusType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WebPageMapperTest extends ContextLoad {

    private static final int ID = 1;
    private static final String PATH = "https://github.com/Filkoof";
    private static final int CODE = 200;
    private static final String CONTENT = "<!DOCTYPE html><html><head>";
    private final PageMapper pageMapper = Mappers.getMapper(PageMapper.class);

    @Test
    @DisplayName("Map Entity to DTO")
    void pageEntityToDto() {
        assertNotNull(pageMapper);

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setId(ID)
                .setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError("")
                .setUrl("")
                .setName("");

        PageEntity pageEntity = new PageEntity();
        pageEntity.setId(ID).setSite(siteEntity).setPath(PATH).setCode(CODE).setContent(CONTENT);

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

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setId(ID)
                .setStatus(StatusType.INDEXED)
                .setStatusTime(LocalDateTime.now())
                .setLastError("")
                .setUrl("")
                .setName("");

        PageEntity pageEntity = pageMapper.pageDtoToEntity(pageDto, siteEntity);

        assertAll(() -> assertEquals(pageDto.getId(), pageEntity.getId()),
                () -> assertEquals(pageDto.getSiteId(), pageEntity.getSite().getId()),
                () -> assertEquals(pageDto.getPath(), pageEntity.getPath()),
                () -> assertEquals(pageDto.getCode(), pageEntity.getCode()),
                () -> assertEquals(pageDto.getContent(), pageEntity.getContent())
        );
    }
}