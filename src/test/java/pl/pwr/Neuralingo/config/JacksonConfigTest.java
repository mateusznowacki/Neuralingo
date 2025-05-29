package pl.pwr.Neuralingo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonConfigTest {

    public static class TestDto {
        public LocalDate date;

        public TestDto() {}

        public TestDto(LocalDate date) {
            this.date = date;
        }
    }

    @Test
    public void testObjectMapperConfiguration() throws Exception {
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();

        // Verify serialization of LocalDate
        TestDto dto = new TestDto(LocalDate.of(2023, 5, 28));
        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).contains("\"date\":\"2023-05-28\"");

        // Verify deserialization of LocalDate
        TestDto deserialized = objectMapper.readValue("{\"date\":\"2023-05-28\"}", TestDto.class);
        assertThat(deserialized.date).isEqualTo(LocalDate.of(2023, 5, 28));

        // Verify configuration flags
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }
}
