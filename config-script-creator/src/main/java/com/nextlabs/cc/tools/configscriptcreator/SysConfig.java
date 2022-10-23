package com.nextlabs.cc.tools.configscriptcreator;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The JSON file contains a list of objects from this class.
 *
 * @author Sachindra Dasun
 */
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 6740001711769825405L;

    @JsonIgnore
    private long id;
    private String application;
    private String configKey;
    private String value;
    private String defaultValue;
    private String valueFormat;
    private String mainGroup;
    private String subGroup;
    private long mainGroupOrder;
    private long subGroupOrder;
    private long configOrder;
    private boolean hidden;
    private boolean readOnly;
    private boolean advanced;
    private boolean ui;
    private boolean encrypted;
    private boolean restartRequired;
    private String dataType;
    private String fieldType;
    private String options;
    private boolean required;
    private String pattern;
    private String description;
    private DBType dbType;
    @JsonIgnore
    private Date modifiedOn;
    private long modifiedBy;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(String valueFormat) {
        this.valueFormat = valueFormat;
    }

    public String getMainGroup() {
        return mainGroup;
    }

    public void setMainGroup(String mainGroup) {
        this.mainGroup = mainGroup;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public long getMainGroupOrder() {
		return mainGroupOrder;
	}

	public void setMainGroupOrder(long mainGroupOrder) {
		this.mainGroupOrder = mainGroupOrder;
	}

	public long getSubGroupOrder() {
		return subGroupOrder;
	}

	public void setSubGroupOrder(long subGroupOrder) {
		this.subGroupOrder = subGroupOrder;
	}

	public long getConfigOrder() {
        return configOrder;
    }

    public void setConfigOrder(long configOrder) {
        this.configOrder = configOrder;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isAdvanced() {
		return advanced;
	}

	public void setAdvanced(boolean advanced) {
		this.advanced = advanced;
	}

	public boolean isUi() {
        return ui;
    }

    public void setUi(boolean ui) {
        this.ui = ui;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isRestartRequired() {
        return restartRequired;
    }

    public void setRestartRequired(boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String toString() {
        return "SysConfig{" +
                "id=" + id +
                ", application='" + application + '\'' +
                ", configKey='" + configKey + '\'' +
                ", value='" + value + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", valueFormat='" + valueFormat + '\'' +
                ", mainGroup='" + mainGroup + '\'' +
                ", subGroup='" + subGroup + '\'' +
                ", mainGroupOrder=" + mainGroupOrder +
                ", subGroupOrder=" + subGroupOrder +
                ", configOrder=" + configOrder +
                ", hidden=" + hidden +
                ", readOnly=" + readOnly +
                ", ui=" + ui +
                ", encrypted=" + encrypted +
                ", restartRequired=" + restartRequired +
                ", dataType='" + dataType + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", options='" + options + '\'' +
                ", required=" + required +
                ", pattern='" + pattern + '\'' +
                ", description='" + description + '\'' +
                ", modifiedOn=" + modifiedOn +
                ", modifiedBy=" + modifiedBy +
                '}';
    }
}
