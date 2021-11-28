package com.fadeto.gpubot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import model.Chat;
import model.GpuData;
import model.Info;
import model.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Slf4j
@SpringBootApplication
public class GpuBotApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(GpuBotApplication.class, args);

        String chat = readFileToString("files/result.json");

        JsonMapper jsonMapper = JsonMapper.builder()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        Chat parseChat = jsonMapper.readValue(chat, Chat.class);

        Calendar c = GregorianCalendar.getInstance();
        c.set(2021, Calendar.JANUARY,1);
        HashMap<String, List<String>> gpuMap;
        List<Message> collect = parseChat.getMessages().stream()
                .filter(message -> message.getDate().after(c.getTime()))
                .filter(message -> message.getText() instanceof ArrayList)
                .collect(toList());

        List<GpuData> gpuDataList = collect.stream().map(data -> createGpuData(data))
                .sorted(Comparator.comparing(GpuData::getUrl))
                .collect(toList());
        Map<String, List<String>> collect1 = gpuDataList.stream()
                .collect(groupingBy(GpuData::getModelName, mapping(GpuData::getUrl, toList())));

        log.info("Data: {}", collect);
    }

    private static GpuData createGpuData(Message data) {
        return GpuData.builder()
                .id(data.getId())
                .modelName(extractModelName((List<Object>) data.getText()))
                .url(extractUrl((List<Object>) data.getText()))
                .build();
    }

    private static String extractUrl(List<Object> text) {
        return text.stream()
                .filter(data -> data instanceof Info)
                .filter(data -> ((Info) data).getType().equals("link"))
                .map(data -> ((Info) data).getText()).findFirst().orElse(null);

    }

    private static String extractModelName(List<Object> text) {

        String modelName = "";

        for (Object o : text) {
            if (o instanceof String) {
                modelName = extractModelNameFromString((String) o);
            } else {
                modelName = extractModelNameFromString(((Info) o).getText());
            }
            if (!modelName.isBlank()) {
                return modelName;
            }
        }
        return modelName;
    }

    private static String extractModelNameFromString(String text) {
        if (text.contains("3070 Ti")) {
            return "3070 Ti";
        } else if (text.contains("3080 Ti")){
            return "3080 Ti";
        } else if (text.contains("3060 Ti")){
            return "3060 Ti";
        } else if (text.contains("3080")){
            return "3080";
        } else if (text.contains("3070")){
            return "3070";
        } else if (text.contains("3060")){
            return "3060";
        } else if (text.contains("3090")){
            return "3090";
        } else {
            return "";
        }

    }

//    private static String findOfferName(List<Object> offer) {
//        offer.stream().filter(offer instanceof Array)
//    }

    public static String readFileToString(String path) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        return asString(resource);
    }

    public static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
