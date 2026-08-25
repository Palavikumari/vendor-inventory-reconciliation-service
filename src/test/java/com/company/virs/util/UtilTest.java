package com.company.virs.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void generateUUID_ShouldReturnValue() {

        UUID uuid =
                CommonUtil.generateUUID();

        assertNotNull(uuid);
    }

    @Test
    void generateUUID_ShouldGenerateUniqueValues() {

        UUID first =
                CommonUtil.generateUUID();

        UUID second =
                CommonUtil.generateUUID();

        assertNotEquals(
                first,
                second
        );
    }

    // -------------------------
    // ValidationUtil
    // -------------------------

    @Test
    void isPositive_ShouldReturnTrue_WhenZero() {

        assertTrue(
                ValidationUtil.isPositive(0)
        );
    }

    @Test
    void isPositive_ShouldReturnTrue_WhenPositive() {

        assertTrue(
                ValidationUtil.isPositive(10)
        );
    }

    @Test
    void isPositive_ShouldReturnFalse_WhenNegative() {

        assertFalse(
                ValidationUtil.isPositive(-1)
        );
    }

    @Test
    void isPositive_ShouldReturnFalse_WhenNull() {

        assertFalse(
                ValidationUtil.isPositive(null)
        );
    }

    @Test
    void isNotBlank_ShouldReturnTrue() {

        assertTrue(
                ValidationUtil.isNotBlank(
                        "value"
                )
        );
    }

    @Test
    void isNotBlank_ShouldReturnFalse_WhenNull() {

        assertFalse(
                ValidationUtil.isNotBlank(null)
        );
    }

    @Test
    void isNotBlank_ShouldReturnFalse_WhenEmpty() {

        assertFalse(
                ValidationUtil.isNotBlank("")
        );
    }

    @Test
    void isNotBlank_ShouldReturnFalse_WhenWhitespace() {

        assertFalse(
                ValidationUtil.isNotBlank("   ")
        );
    }

    @Test
    void now_ShouldReturnCurrentTime() {

        LocalDateTime now =
                DateUtil.now();

        assertNotNull(now);
    }

    @Test
    void format_ShouldReturnEmpty_WhenNull() {

        assertEquals(
                "",
                DateUtil.format(null)
        );
    }

    @Test
    void format_ShouldFormatDate() {

        LocalDateTime date =
                LocalDateTime.of(
                        2025,
                        1,
                        1,
                        10,
                        20,
                        30
                );

        assertEquals(
                "2025-01-01 10:20:30",
                DateUtil.format(date)
        );
    }

    @Test
    void isCsv_ShouldReturnTrue() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "data".getBytes()
                );

        assertTrue(
                FileUtil.isCsv(file)
        );
    }

    @Test
    void isCsv_ShouldReturnFalse_WhenWrongExtension() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.txt",
                        "text/plain",
                        "data".getBytes()
                );

        assertFalse(
                FileUtil.isCsv(file)
        );
    }

    @Test
    void isCsv_ShouldReturnFalse_WhenNullFile() {

        assertFalse(
                FileUtil.isCsv(null)
        );
    }

    @Test
    void getFileName_ShouldReturnName() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "data".getBytes()
                );

        assertEquals(
                "inventory.csv",
                FileUtil.getFileName(file)
        );
    }

    @Test
    void getFileName_ShouldReturnEmpty_WhenNull() {

        assertEquals(
                "",
                FileUtil.getFileName(null)
        );
    }
}