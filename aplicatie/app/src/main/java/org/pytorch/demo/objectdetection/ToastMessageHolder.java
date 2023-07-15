package org.pytorch.demo.objectdetection;

public class ToastMessageHolder {
    private static final ToastMessageHolder instance = new ToastMessageHolder();
    private String toastMessage;

    private ToastMessageHolder() {}

    public static ToastMessageHolder getInstance() {
        return instance;
    }

    public String getToastMessage() {
        return toastMessage;
    }

    public void setToastMessage(String message) {
        toastMessage = message;
    }
}
