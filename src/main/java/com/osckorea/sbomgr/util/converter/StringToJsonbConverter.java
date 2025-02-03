package com.osckorea.sbomgr.util.converter;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.sql.SQLException;

@WritingConverter
public class StringToJsonbConverter implements Converter<String, PGobject>{

    @Override
    public PGobject convert(String source) {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(source);
            return jsonObject;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to convert String to PGobject", e);
        }
    }

}
