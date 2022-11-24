package searchengine.mapper;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.ActiveProfiles;
import searchengine.dto.PageDto;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.enumerated.StatusType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class WebPageMapperTest {

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