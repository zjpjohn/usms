/*
 * Copyright (c) 2005, 2017, EVECOM Technology Co.,Ltd. All rights reserved.
 * EVECOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package net.evecom.common.usms.core.model;

import net.sf.json.JSONObject;

/**
 * 描述
 *
 * @author Wash Wang
 * @version 1.0
 * @created 2017/5/9 10:42
 */
public class ErrorStatus {

    public static final String INVALID_PARAMS = "invalid_params";
    /**
     * 错误
     */
    private String error;

    /**
     * 错误描述
     */
    private String errorDescription;

    private String body = "";

    public String getBody() {
        return body;
    }

    public static class Builder {
        /**
         * 错误
         */
        private String error = "";

        /**
         * 错误描述
         */
        private String errorDescription = "";

        public Builder(String error, String errorDescription) {
            this.error = error;
            this.errorDescription = errorDescription;
        }

        public Builder setError(String error) {
            this.error = error;
            return this;
        }

        public Builder setErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
            return this;
        }

        public ErrorStatus buildJSONMessage() {
            return new ErrorStatus(this);
        }
    }

    public ErrorStatus(Builder builder) {
        this.error = builder.error;
        this.errorDescription = builder.errorDescription;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", this.error);
        jsonObject.put("error_description", this.errorDescription);
        this.body = jsonObject.toString();
    }
}
