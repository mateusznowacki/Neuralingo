package pl.pwr.Neuralingo.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import pl.pwr.Neuralingo.dto.document.content.AzureAnalyzeResultDto;

import java.io.IOException;

public class AzureAnalyzeResultMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AzureAnalyzeResultDto mapJsonToDto(String json) throws IOException {
        return objectMapper.readValue(json, AzureAnalyzeResultDto.class);
    }
}
