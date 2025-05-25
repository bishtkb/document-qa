package com.project.document_qa.enums;

public class ApiResponseCode {

    public enum Login implements ResponseCode {
        SUCCESS(0, "SUCCESS"),
        ERROR(1, "ERROR"),
        UNAUTHENTICATED(3, "UNAUTHENTICATED");

        private final int code;
        private final String message;

        Login(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    public enum User implements ResponseCode {
        USER_NOT_FOUND(101, "USER_NOT_FOUND");

        private final int code;
        private final String message;

        User(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    public enum Document implements ResponseCode {
        DOCUMENT_NOT_FOUND(201, "DOCUMENT_NOT_FOUND");

        private final int code;
        private final String message;

        Document(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
