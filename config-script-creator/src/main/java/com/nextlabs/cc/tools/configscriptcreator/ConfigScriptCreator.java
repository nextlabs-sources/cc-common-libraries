package com.nextlabs.cc.tools.configscriptcreator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ComparisonChain;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.text.StringSubstitutor;

/**
 * A tool to create database scripts for system configurations. Also can be used to create csv/json files.
 *
 * @author Sachindra Dasun
 */
public class ConfigScriptCreator {

    private final static String INPUT_FILE_NAME = "configs.json";
    private final static FileFormat OUTPUT_FORMAT = FileFormat.SQL;

    private enum FileFormat {
        CSV("csv"), JSON("json"), SQL("sql");

        private String extension;

        FileFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    private static final String DB2_CONFIG_SQL_FORMAT = "MERGE INTO SYS_CONFIG USING (SELECT 1 FROM SYSIBM.SYSDUMMY1) ON (APPLICATION = '${application}' AND CONFIG_KEY = '${configKey}')\n" +
            "WHEN MATCHED THEN UPDATE SET VALUE = '${value}', DEFAULT_VALUE = '${defaultValue}', VALUE_FORMAT = '${valueFormat}', MAIN_GROUP = '${mainGroup}', SUB_GROUP = '${subGroup}', MAIN_GROUP_ORDER = '${mainGroupOrder}', SUB_GROUP_ORDER = '${subGroupOrder}', CONFIG_ORDER = '${configOrder}', HIDDEN = '${hidden}', READ_ONLY = '${readOnly}', ADVANCED = '${advanced}', UI = '${ui}', ENCRYPTED = '${encrypted}', RESTART_REQUIRED = '${restartRequired}', DATA_TYPE = '${dataType}', FIELD_TYPE = '${fieldType}' , OPTIONS = '${options}', REQUIRED = '${required}', PATTERN = '${pattern}', DESCRIPTION = '${description}', MODIFIED_ON = CURRENT_TIMESTAMP, MODIFIED_BY = '${modifiedBy}'\n" +
            "WHEN NOT MATCHED THEN INSERT (ID, APPLICATION, CONFIG_KEY, VALUE, DEFAULT_VALUE, VALUE_FORMAT, MAIN_GROUP, SUB_GROUP, MAIN_GROUP_ORDER, SUB_GROUP_ORDER, CONFIG_ORDER, HIDDEN, READ_ONLY, ADVANCED, UI, ENCRYPTED, RESTART_REQUIRED, DATA_TYPE, FIELD_TYPE, OPTIONS, REQUIRED, PATTERN, DESCRIPTION, MODIFIED_ON, MODIFIED_BY ) VALUES (HIBERNATE_SEQUENCE.NEXTVAL, '${application}', '${configKey}', '${value}', '${defaultValue}', '${valueFormat}', '${mainGroup}', '${subGroup}', '${mainGroupOrder}', '${subGroupOrder}', '${configOrder}', '${hidden}', '${readOnly}', '${advanced}', '${ui}', '${encrypted}', '${restartRequired}', '${dataType}', '${fieldType}', '${options}', '${required}', '${pattern}', '${description}', CURRENT_TIMESTAMP, '${modifiedBy}' );";

    private static final String ORACLE_CONFIG_SQL_FORMAT = "MERGE INTO SYS_CONFIG USING (SELECT 1 FROM DUAL) ON (APPLICATION = '${application}' AND CONFIG_KEY = '${configKey}')\n" +
            "WHEN MATCHED THEN UPDATE SET VALUE = '${value}', DEFAULT_VALUE = '${defaultValue}', VALUE_FORMAT = '${valueFormat}', MAIN_GROUP = '${mainGroup}', SUB_GROUP = '${subGroup}', MAIN_GROUP_ORDER = '${mainGroupOrder}', SUB_GROUP_ORDER = '${subGroupOrder}', CONFIG_ORDER = '${configOrder}', HIDDEN = '${hidden}', READ_ONLY = '${readOnly}', ADVANCED = '${advanced}', UI = '${ui}', ENCRYPTED = '${encrypted}', RESTART_REQUIRED = '${restartRequired}', DATA_TYPE = '${dataType}', FIELD_TYPE = '${fieldType}' , OPTIONS = '${options}', REQUIRED = '${required}', PATTERN = '${pattern}', DESCRIPTION = '${description}', MODIFIED_ON = CURRENT_TIMESTAMP, MODIFIED_BY = '${modifiedBy}'\n" +
            "WHEN NOT MATCHED THEN INSERT (ID, APPLICATION, CONFIG_KEY, VALUE, DEFAULT_VALUE, VALUE_FORMAT, MAIN_GROUP, SUB_GROUP, MAIN_GROUP_ORDER, SUB_GROUP_ORDER, CONFIG_ORDER, HIDDEN, READ_ONLY, ADVANCED, UI, ENCRYPTED, RESTART_REQUIRED, DATA_TYPE, FIELD_TYPE, OPTIONS, REQUIRED, PATTERN, DESCRIPTION, MODIFIED_ON, MODIFIED_BY ) VALUES (HIBERNATE_SEQUENCE.NEXTVAL, '${application}', '${configKey}', '${value}', '${defaultValue}', '${valueFormat}', '${mainGroup}', '${subGroup}', '${mainGroupOrder}', '${subGroupOrder}', '${configOrder}', '${hidden}', '${readOnly}', '${advanced}', '${ui}', '${encrypted}', '${restartRequired}', '${dataType}', '${fieldType}', '${options}', '${required}', '${pattern}', '${description}', CURRENT_TIMESTAMP, '${modifiedBy}' );";

    private static final String SQL_SERVER_CONFIG_SQL_FORMAT = "MERGE INTO SYS_CONFIG SC1 USING (SELECT '${application}' AS APPLICATION, '${configKey}' AS CONFIG_KEY) AS SC2 ON (SC1.APPLICATION = SC2.APPLICATION AND SC1.CONFIG_KEY = SC2.CONFIG_KEY)\n" +
            "WHEN MATCHED THEN UPDATE SET VALUE = '${value}', DEFAULT_VALUE = '${defaultValue}', VALUE_FORMAT = '${valueFormat}', MAIN_GROUP = '${mainGroup}', SUB_GROUP = '${subGroup}', MAIN_GROUP_ORDER = '${mainGroupOrder}', SUB_GROUP_ORDER = '${subGroupOrder}', CONFIG_ORDER = '${configOrder}', HIDDEN = '${hidden}', READ_ONLY = '${readOnly}', ADVANCED = '${advanced}', UI = '${ui}', ENCRYPTED = '${encrypted}', RESTART_REQUIRED = '${restartRequired}', DATA_TYPE = '${dataType}', FIELD_TYPE = '${fieldType}' , OPTIONS = '${options}', REQUIRED = '${required}', PATTERN = '${pattern}', DESCRIPTION = '${description}', MODIFIED_ON = CURRENT_TIMESTAMP, MODIFIED_BY = '${modifiedBy}'\n" +
            "WHEN NOT MATCHED THEN INSERT (APPLICATION, CONFIG_KEY, VALUE, DEFAULT_VALUE, VALUE_FORMAT, MAIN_GROUP, SUB_GROUP, MAIN_GROUP_ORDER, SUB_GROUP_ORDER, CONFIG_ORDER, HIDDEN, READ_ONLY, ADVANCED, UI, ENCRYPTED, RESTART_REQUIRED, DATA_TYPE, FIELD_TYPE, OPTIONS, REQUIRED, PATTERN, DESCRIPTION, MODIFIED_ON, MODIFIED_BY ) VALUES ('${application}', '${configKey}', '${value}', '${defaultValue}', '${valueFormat}', '${mainGroup}', '${subGroup}', '${mainGroupOrder}', '${subGroupOrder}', '${configOrder}', '${hidden}', '${readOnly}', '${advanced}', '${ui}', '${encrypted}', '${restartRequired}', '${dataType}', '${fieldType}', '${options}', '${required}', '${pattern}', '${description}', CURRENT_TIMESTAMP, '${modifiedBy}' )&semi;";

    private static final String POSTGRESQL_CONFIG_SQL_FORMAT = "INSERT INTO SYS_CONFIG (ID, APPLICATION, CONFIG_KEY, VALUE, DEFAULT_VALUE, VALUE_FORMAT, MAIN_GROUP, SUB_GROUP, MAIN_GROUP_ORDER, SUB_GROUP_ORDER, CONFIG_ORDER, HIDDEN, READ_ONLY, ADVANCED, UI, ENCRYPTED, RESTART_REQUIRED, DATA_TYPE, FIELD_TYPE, OPTIONS, REQUIRED, PATTERN, DESCRIPTION, MODIFIED_ON, MODIFIED_BY ) VALUES (NEXTVAL('HIBERNATE_SEQUENCE'), '${application}', '${configKey}', '${value}', '${defaultValue}', '${valueFormat}', '${mainGroup}', '${subGroup}', '${mainGroupOrder}', '${subGroupOrder}', '${configOrder}', '${hidden}', '${readOnly}', '${advanced}', '${ui}', '${encrypted}', '${restartRequired}', '${dataType}', '${fieldType}', '${options}', '${required}', '${pattern}', '${description}', CURRENT_TIMESTAMP, '${modifiedBy}' )\n" +
            "ON CONFLICT (APPLICATION, CONFIG_KEY) DO UPDATE SET VALUE = '${value}', DEFAULT_VALUE = '${defaultValue}', VALUE_FORMAT = '${valueFormat}', MAIN_GROUP = '${mainGroup}', SUB_GROUP = '${subGroup}', MAIN_GROUP_ORDER = '${mainGroupOrder}', SUB_GROUP_ORDER = '${subGroupOrder}', CONFIG_ORDER = '${configOrder}', HIDDEN = '${hidden}', READ_ONLY = '${readOnly}', ADVANCED = '${advanced}', UI = '${ui}', ENCRYPTED = '${encrypted}', RESTART_REQUIRED = '${restartRequired}', DATA_TYPE = '${dataType}', FIELD_TYPE = '${fieldType}' , OPTIONS = '${options}', REQUIRED = '${required}', PATTERN = '${pattern}', DESCRIPTION = '${description}', MODIFIED_ON = CURRENT_TIMESTAMP, MODIFIED_BY = '${modifiedBy}';";

    private static final String FILE_NAME_PATTERN = "configs_%s_%s.%s";

    private static String timestamp;

    public static void main(String[] args) throws Exception {
        timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        List<SysConfig> sysConfigs = readInputFile();
        sysConfigs.sort((o1, o2) -> ComparisonChain.start()
                .compare(o1.getApplication(), o2.getApplication())
                .compare(o1.getConfigKey(), o2.getConfigKey())
                .result());
        createFiles(sysConfigs);
    }

    private static List<SysConfig> readInputFile() throws IOException {
        File configDataFile = new File(Objects.requireNonNull(ConfigScriptCreator.class.getClassLoader()
                .getResource(INPUT_FILE_NAME)).getFile());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(configDataFile, new TypeReference<List<SysConfig>>() {
        });
    }

    private static void createFiles(List<SysConfig> sysConfigs) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IllegalAccessException {
        File outputDir = crateOutputDir();
        switch (OUTPUT_FORMAT) {
            case CSV: {
                Path outputFile = Paths.get(outputDir.getPath(), String.format(FILE_NAME_PATTERN, "csv", timestamp, OUTPUT_FORMAT.getExtension()));
                Writer writer = new FileWriter(outputFile.toFile());
                new StatefulBeanToCsvBuilder<SysConfig>(writer).build().write(sysConfigs);
                writer.close();
                System.out.println(String.format("Config script saved to %s", outputFile.toAbsolutePath()));
                break;
            }
            case JSON: {
                Path outputFile = Paths.get(outputDir.getPath(), String.format(FILE_NAME_PATTERN, "json", timestamp, OUTPUT_FORMAT.getExtension()));
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(outputFile.toFile(), sysConfigs);
                System.out.println(String.format("Config script saved to %s", outputFile.toAbsolutePath()));
                break;
            }
            case SQL: {
                Map<String, String> scripts = createScripts(sysConfigs);
                for (Map.Entry<String, String> entry : scripts.entrySet()) {
                    Path outputFile = Paths.get(outputDir.getPath(), String.format(FILE_NAME_PATTERN, entry.getKey(), timestamp, OUTPUT_FORMAT.getExtension()));
                    Files.write(outputFile, entry.getValue().getBytes(), StandardOpenOption.CREATE);
                    System.out.println(String.format("Config script for %s saved to %s", entry.getKey(), outputFile.toAbsolutePath()));
                }
                break;

            }
        }
    }

    private static Map<String, String> createScripts(List<SysConfig> sysConfigs) throws IllegalAccessException {
        Map<String, String> scripts = new HashMap<>();
        StringBuilder db2Script = new StringBuilder();
        StringBuilder oracleScript = new StringBuilder();
        StringBuilder sqlServerScript = new StringBuilder();
        StringBuilder postgreSQLScript = new StringBuilder();
        for (SysConfig sysConfig : sysConfigs) {
            Map<String, String> sysConfigValues = new HashMap<>();
            Field[] fields = sysConfig.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(sysConfig);
                String value = "";
                if (fieldValue != null) {
                    value = fieldValue.toString().replaceAll("'", "''").replaceAll(";", "&semi");
                    if (field.getType().equals(boolean.class)) {
                        value = field.getBoolean(sysConfig) ? "1" : "0";
                    }
                }
                sysConfigValues.put(field.getName(), value);
            }
            StringSubstitutor stringSubstitutor = new StringSubstitutor(sysConfigValues);
            if (sysConfig.getDbType() == null || DBType.DB2.equals(sysConfig.getDbType())) {
                db2Script.append((db2Script.length() == 0) ? "" : System.lineSeparator()
                        + System.lineSeparator()).append(stringSubstitutor.replace(DB2_CONFIG_SQL_FORMAT));
            }
            if (sysConfig.getDbType() == null || DBType.ORACLE.equals(sysConfig.getDbType())) {
                oracleScript.append((oracleScript.length() == 0) ? "" : System.lineSeparator()
                        + System.lineSeparator()).append(stringSubstitutor.replace(ORACLE_CONFIG_SQL_FORMAT));
            }
            if (sysConfig.getDbType() == null || DBType.SQL_SERVER.equals(sysConfig.getDbType())) {
                sqlServerScript.append((sqlServerScript.length() == 0) ? "" : System.lineSeparator()
                        + System.lineSeparator()).append(stringSubstitutor.replace(SQL_SERVER_CONFIG_SQL_FORMAT));
            }
            if (sysConfig.getDbType() == null || DBType.POSTGRESQL.equals(sysConfig.getDbType())) {
                postgreSQLScript.append((postgreSQLScript.length() == 0) ? "" : System.lineSeparator()
                        + System.lineSeparator()).append(stringSubstitutor.replace(POSTGRESQL_CONFIG_SQL_FORMAT));
            }

            System.out.println("Scripts created for " + sysConfig.getConfigKey());
        }
        System.out.println("==================================================");
        scripts.put("db2", db2Script.toString());
        scripts.put("oracle", oracleScript.toString());
        scripts.put("sqlserver", sqlServerScript.toString());
        scripts.put("postgresql", postgreSQLScript.toString());
        return scripts;
    }

    private static File crateOutputDir() {
        File outputDir = new File("scripts");
        boolean created = outputDir.mkdir();
        if (created) {
            System.out.println("Output folder created at: " + outputDir.getAbsolutePath());
        }
        return outputDir;
    }

    private static SysConfig getSampleConfig() {
        SysConfig sysConfig = new SysConfig();
        sysConfig.setApplication("console");
        sysConfig.setConfigKey("config.key");
        sysConfig.setValue("config.value");
        sysConfig.setDefaultValue("config.value");
        sysConfig.setValueFormat("");
        sysConfig.setMainGroup("default");
        sysConfig.setSubGroup("default");
        sysConfig.setMainGroupOrder(0);
        sysConfig.setSubGroupOrder(0);
        sysConfig.setConfigOrder(0);
        sysConfig.setHidden(false);
        sysConfig.setReadOnly(false);
        sysConfig.setAdvanced(false);
        sysConfig.setUi(false);
        sysConfig.setEncrypted(false);
        sysConfig.setRestartRequired(true);
        sysConfig.setDataType("text");
        sysConfig.setFieldType("text");
        sysConfig.setOptions("");
        sysConfig.setRequired(true);
        sysConfig.setPattern("");
        sysConfig.setDescription("This configuration is used to set " + sysConfig.getConfigKey());
        sysConfig.setModifiedBy(-1);
        return sysConfig;
    }

}
