package eu.sshopencloud.marketplace.controllers;

public class PageTooLargeException extends Exception {

    public PageTooLargeException(int max) {
        super("Page size must not be greater than "+ max + "!");
    }

}
