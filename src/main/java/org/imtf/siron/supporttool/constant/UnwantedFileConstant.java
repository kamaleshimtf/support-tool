package com.imtf.cstool.supporttool.constant;


import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class UnwantedFileConstant {

    private final List<String> FILE_CONTENT = new ArrayList<>();
    private static final String FILE_CONTENT_VALUE = "*****";

    public UnwantedFileConstant() {
        FILE_CONTENT.add("PASSWORD");
        FILE_CONTENT.add("PASSWD");
        FILE_CONTENT.add("PWD");
        FILE_CONTENT.add("PASSWORT");
        FILE_CONTENT.add("URL");
        FILE_CONTENT.add("KEY_FILE");
        FILE_CONTENT.add("CRYPT");
        FILE_CONTENT.add("DB.PASS");
        FILE_CONTENT.add("SECRET");
        FILE_CONTENT.add("KYC_JMSPASS");

        List<Pattern> FILE_CONTENT_PATTERN = FILE_CONTENT.stream()
                .map(fileContent -> Pattern.compile(Pattern.quote(fileContent)))
                .collect(Collectors.toList());

        FILE_CONTENT_PATTERN.add(
                Pattern.compile("(USER)(?!.*EXIT.*)(?!.*_CONSORTIUM_SERVER.*)")
        );
    }

    public boolean isSensitive(String fileContentKey){
        return FILE_CONTENT.stream().anyMatch(fileContent -> fileContent.matches(fileContentKey));
    }

    public String maskSensitiveLine(String line) {
        String[] parts = line.split("=", 2);

        if (parts.length == 2) {
            return parts[0].trim() + "=" + ProductConstant.SENSITIVE_CONTENT;
        } else {
            return ProductConstant.SENSITIVE_CONTENT;
        }
    }
}
