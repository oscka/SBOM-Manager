package com.osckorea.sbommanager.config;

import com.osckorea.sbommanager.util.converter.JsonbToStringConverter;
import com.osckorea.sbommanager.util.converter.StringToJsonbConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableJdbcRepositories(basePackages = "com.osckorea.sbommanager.repository")
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Override
    protected List<?> userConverters() {
        return Arrays.asList(
                new StringToJsonbConverter(),
                new JsonbToStringConverter()
        );
    }

}
